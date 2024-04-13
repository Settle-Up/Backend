package settleup.backend.domain.user.service.Impl;



import io.sentry.Sentry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.EmailSenderService;
import settleup.backend.global.config.EmailSendConfig;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

@Service
@Transactional
@RequiredArgsConstructor
public class EmailSenderServiceImpl implements EmailSenderService {

    private final JavaMailSender mailSender;
    private final EmailSendConfig emailConfig;
    private static final Logger log = LoggerFactory.getLogger(EmailSenderServiceImpl.class);

    @Override
    public CompletableFuture<Void> sendEmailToNewGroupUser(CreateGroupResponseDto newUserGroupInfo) {
        return CompletableFuture.runAsync(() -> {
            for (UserInfoDto recipient : newUserGroupInfo.getUserList()) {
                try {
                    sendEmail(recipient, newUserGroupInfo.getGroupName());
                } catch (MessagingException e) {
                    throw new RuntimeException(e);
                }
            }
        }).exceptionally(ex -> {
            Sentry.captureException(ex);
            return null;
        });
    }

    private void sendEmail(UserInfoDto recipient, String groupName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        message.setFrom(new InternetAddress(emailConfig.getUserName()));
        message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress(recipient.getUserEmail()));
        message.setSubject(String.format("Hello %s, it's Settle-Up Information notices", recipient.getUserName()));
        message.setText(String.format("You have been invited to the new group, %s, on Settle-Up. Easily manage group settlements with Settle-Up. "
                + "%s의 새로운 그룹에 초대되었습니다. 어려운 그룹 정산 settle-up에서 간편하게 하세요.", groupName, groupName));

        mailSender.send(message);
        log.info("Message sent successfully to {}", recipient.getUserEmail());
    }
}
