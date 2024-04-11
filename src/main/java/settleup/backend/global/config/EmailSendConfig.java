package settleup.backend.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class EmailSendConfig {
    @Value("${google.mail.user_name}")
    private String userName;

    @Value("${google.mail.password}")
    private String password;

    @Value("${google.mail.smtp.host}")
    private String host;

    @Value("${google.mail.smtp.port}")
    private String port;

    @Value("${google.mail.smtp.auth}")
    private String auth;

    @Value("${google.mail.smtp.starttls_enable}")
    private String starttls_enable;
}
