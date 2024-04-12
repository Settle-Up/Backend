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
        mailSender.setPort(Integer.parseInt(emailConfig.getPort())); // 이미 int로 처리되어야 함
        mailSender.setUsername(emailConfig.getUserName());
        mailSender.setPassword(emailConfig.getPassword());
        mailSender.setDefaultEncoding("UTF-8");
        mailSender.setProtocol("smtp");

        Properties props = new Properties();
        props.put("mail.smtp.auth", emailConfig.getAuth());  // boolean 처리
        props.put("mail.smtp.starttls.enable", emailConfig.getStarttlsEnable());  // boolean 처리
        mailSender.setJavaMailProperties(props);

        return mailSender;
    }
}
