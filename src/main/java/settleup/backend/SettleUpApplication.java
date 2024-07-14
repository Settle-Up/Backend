package settleup.backend;

import io.sentry.Sentry;
import io.sentry.IHub;
import io.sentry.spring.jakarta.SentryExceptionResolver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import settleup.backend.global.Util.CustomTransactionNameProvider;

@EnableScheduling
@SpringBootApplication
public class SettleUpApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(SettleUpApplication.class, args);
        } catch (Exception e) {
            Sentry.captureException(e); // Sentry로 예외 전송
            throw e; // 예외를 다시 던져 Spring Boot가 처리하도록 합니다
        }
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", "https://3dc4-125-132-224-129.ngrok-free.app") // Allowed origins
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH") // Allowed methods
                        .allowCredentials(true) // Allow credentials
                        .allowedHeaders("*") // Allow all headers
                        .exposedHeaders("Authorization", "Content-Type") // Expose headers
                        .maxAge(3600); // Cache preflight response for 1 hour
            }
        };
    }

    @Bean
    public IHub sentryHub() {
        return Sentry.getCurrentHub();
    }

    @Bean
    public HandlerExceptionResolver sentryExceptionResolver(IHub sentryHub, CustomTransactionNameProvider customTransactionNameProvider) {
        return new SentryExceptionResolver(sentryHub, customTransactionNameProvider, 0);
    }
}
