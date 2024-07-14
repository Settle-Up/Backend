package settleup.backend.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import settleup.backend.domain.user.repository.DemoUserRepository;
import settleup.backend.global.Util.RateLimitingProvider;

@Getter
@Component
public class RateLimitingConfig {

    @Value("${rate.limiting.time-limit-minutes}")
    private long timeLimitMinutes;
}


