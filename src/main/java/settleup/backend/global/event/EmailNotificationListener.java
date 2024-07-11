package settleup.backend.global.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.user.service.EmailSenderService;

import java.util.concurrent.CompletableFuture;

@Component
public class EmailNotificationListener {

    private final EmailSenderService emailService;

    public EmailNotificationListener(EmailSenderService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handleCreateGroupEvent(InviteGroupEvent event) {
        emailService.sendEmailToNewGroupUser(event.getGroupInfo()).join(); // Wait for all emails to be sent
    }
}

