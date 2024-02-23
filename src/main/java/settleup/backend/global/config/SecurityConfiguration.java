package settleup.backend.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // @formatter:off
        http
                .csrf(csrf -> csrf.ignoringRequestMatchers("/auth/**") // 특정 경로에 대해 CSRF 보호 비활성화

                )
                .authorizeHttpRequests((authorize) -> authorize
                        .anyRequest().permitAll() // 모든 요청에 대해 인증 없이 접근 허용
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // 필요시 세션 생성
                );
        // @formatter:on
        return http.build();
    }
}