package settleup.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
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
                // 모든 출처를 허용합니다.
                registry.addMapping("/**")
                        .allowedOrigins("http://192.168.0.232:3000")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowCredentials(true) // 인증 정보를 허용합니다. 필요하지 않다면 이 줄을 제거하거나 false로 설정하세요.
                        .allowedHeaders("*") // 모든 헤더를 허용합니다.
                        .exposedHeaders("*") // 클라이언트에서 접근할 수 있는 헤더를 지정합니다.
                        .maxAge(3600); // 브라우저 캐싱 시간 (초 단위)
            }
        };
    }
}
