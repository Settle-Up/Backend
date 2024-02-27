package settleup.backend.domain.user.common;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class UUID_Helper {
    public String UUIDFromEmail(String email) {
        return String.valueOf(UUID.nameUUIDFromBytes(email.getBytes(StandardCharsets.UTF_8)));
    }
}
