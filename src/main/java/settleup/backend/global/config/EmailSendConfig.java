package settleup.backend.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.Getter;

@Component
@Getter
public class EmailSendConfig {
    @Value("${mail.username}")
    private String userName;

    @Value("${mail.password}")
    private String password;

    @Value("${mail.host}")
    private String host;

    @Value("${mail.port}")
    private String port;

    @Value("${mail.default-encoding}")
    private String defaultEncoding;

    @Value("${mail.properties.mail.smtp.auth}")
    private Boolean auth;

    @Value("${mail.properties.mail.smtp.starttls.enable}")
    private Boolean starttlsEnable;

    @Value("${mail.properties.mail.smtp.starttls.required}")
    private Boolean starttlsRequired;

    @Value("${mail.properties.mail.smtp.connectiontimeout}")
    private String connectionTimeout;

    @Value("${mail.properties.mail.smtp.timeout}")
    private String timeout;

    @Value("${mail.properties.mail.smtp.writetimeout}")
    private String writeTimeout;

    @Value("${mail.auth-code-expiration-millis}")
    private long authCodeExpirationMillis;
}


