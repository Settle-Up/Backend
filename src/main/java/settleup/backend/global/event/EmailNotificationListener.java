package settleup.backend.global.event;

import io.sentry.Sentry;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import settleup.backend.domain.user.service.EmailSenderService;

import java.util.concurrent.CompletableFuture;

@Component
public class EmailNotificationListener {

    private EmailSenderService emailService;

    public EmailNotificationListener(EmailSenderService emailService) {
        this.emailService = emailService;
    }

    @Async
    @EventListener
    public void handleCreateGroupEvent(InviteGroupEvent event) {
        CompletableFuture.runAsync(() -> {
            emailService.sendEmailToNewGroupUser(event.getGroupInfo());
        }).exceptionally(ex -> {
            Sentry.captureException(ex);
            return null;
        });
    }
}