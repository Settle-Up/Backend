package settleup.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Properties;

@Configuration
public class MailConfig {
    private final EmailSendConfig emailConfig;

    public MailConfig(EmailSendConfig emailConfig) {
        this.emailConfig = emailConfig;
    }

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(emailConfig.getHost());
        mailSender.setPort(Integer.parseInt(emailConfig.getPort()));
        mailSender.setUsername(emailConfig.getUserName());
        mailSender.setPassword(emailConfig.getPassword());
        mailSender.setDefaultEncoding(emailConfig.getDefaultEncoding());
        mailSender.setProtocol("smtp");

        Properties props = new Properties();
        props.put("mail.smtp.auth", emailConfig.getAuth());
        props.put("mail.smtp.starttls.enable", emailConfig.getStarttlsEnable());
        props.put("mail.smtp.starttls.required", emailConfig.getStarttlsRequired());
        props.put("mail.smtp.connectiontimeout", emailConfig.getConnectionTimeout());
        props.put("mail.smtp.timeout", emailConfig.getTimeout());
        props.put("mail.smtp.writetimeout", emailConfig.getWriteTimeout());
        mailSender.setJavaMailProperties(props);

        return mailSender;
    }
}
