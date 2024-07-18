package settleup.backend.domain.user.service.Impl;



import io.sentry.Sentry;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.user.entity.dto.FeedBackDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.EmailSenderService;
import settleup.backend.global.config.EmailSendConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import settleup.backend.global.exception.CustomException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

;

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
                } catch (MessagingException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).exceptionally(ex -> {
            Sentry.captureException(ex);
            return null;
        });
    }


    private void sendEmail(UserInfoDto recipient, String groupName) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(new InternetAddress(emailConfig.getUserName()));
        helper.setTo(new InternetAddress(recipient.getUserEmail()));
        helper.setSubject(String.format("Hello %s, it's Settle-Up Information notices", recipient.getUserName()));

        String htmlContent = loadHtmlTemplate("emailTemplate.html", recipient.getUserName(), groupName);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Message sent successfully to {}", recipient.getUserEmail());
    }

    private String loadHtmlTemplate(String fileName, String recipientName, String groupName) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("src/main/resources/static/" + fileName)));
        content = content.replace("{{recipientName}}", recipientName);
        content = content.replace("{{groupName}}", groupName);
        return content;
    }


    @Override
    public CompletableFuture<Void> sendFeedBackEmailToManager(FeedBackDto feedBackDto) {
        return CompletableFuture.runAsync(() -> {
            try {
                sendFeedbackEmail(feedBackDto);
            } catch (MessagingException | IOException e) {
                throw new RuntimeException(e);
            }
        }).exceptionally(ex -> {
            log.error("Error sending feedback email", ex);
            return null;
        });
    }

    private void sendFeedbackEmail(FeedBackDto feedBackDto) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(new InternetAddress(emailConfig.getUserName()));
        if (Boolean.TRUE.equals(feedBackDto.getServerOrClient())) {
            helper.setTo(new InternetAddress(emailConfig.getFeedBackReceiverBE()));
            helper.setSubject("SettleUP Backend Feedback");
        } else {
            helper.setTo(new InternetAddress(emailConfig.getFeedBackReceiverFE()));
            helper.setSubject("SettleUP Frontend Feedback");
        }

        String htmlContent = loadHtmlTemplateFeedBack("feedBackEmailTemplate.html", feedBackDto);
        helper.setText(htmlContent, true);

        mailSender.send(message);
        log.info("Feedback email sent successfully");
    }

    private String loadHtmlTemplateFeedBack(String fileName, FeedBackDto feedBackDto) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get("src/main/resources/static/" + fileName)));
        content = content.replace("{{issueLocation}}", feedBackDto.getIssueLocation());
        content = content.replace("{{issueDescription}}", feedBackDto.getIssueDescription());
        content = content.replace("{{replyEmailAddress}}", feedBackDto.getReplyEmailAddress());
        return content;
    }
}