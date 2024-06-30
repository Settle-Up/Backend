package settleup.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@SpringBootApplication
public class SettleUpApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettleUpApplication.class, args);
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000", " https://c2bd-125-132-224-129.ngrok-free.app")
                        .allowedMethods("GET", "POST", "PUT", "DELETE","PATCH")
                        .allowCredentials(true)
                        .allowedHeaders("*")
                        .exposedHeaders("*")
                        .maxAge(5000);
            }
        };
    }
}
