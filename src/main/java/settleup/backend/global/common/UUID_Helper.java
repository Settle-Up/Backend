package settleup.backend.global.common;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Component
public class UUID_Helper {
    public String UUIDFromEmail(String email) {
        return String.valueOf(UUID.nameUUIDFromBytes(email.getBytes(StandardCharsets.UTF_8)));
    }
    public String UUIDForGroup() {
        return String.valueOf(UUID.randomUUID());
    }

    public String UUIDForReceipt() {
        return String.valueOf(UUID.randomUUID());
    }

    public String UUIDForTransaction() {
        return String.valueOf(UUID.randomUUID());
    }
}
