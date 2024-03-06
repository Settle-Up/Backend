package settleup.backend.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class CryptographyConfig {
    @Value("${jwt.secret_key}")
    private String jwtSecretKey;
    @Value("${encryption.secret-key}")
    private String encryptionSecretKey;

}
