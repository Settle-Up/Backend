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
        String prefix = "RT";
        String uniquePart = UUID.randomUUID().toString();
        return prefix + uniquePart;
    }

    public String UUIDForOptimizedTransaction() {
        String prefix = "OPT";
        String uniquePart = UUID.randomUUID().toString();
        return prefix + uniquePart;
    }

    public String UUIDForOptimizedTransactionsDetail() {
        String prefix = "OPTD";
        String uniquePart = UUID.randomUUID().toString();
        return prefix + uniquePart;
    }

    public String UUIDForGroupOptimizedTransactions() {
        String prefix = "GPT";
        String uniquePart = UUID.randomUUID().toString();
        return prefix + uniquePart;
    }

    public String UUIDForGroupOptimizedDetail() {
        String prefix = "GPTD";
        String uniquePart = UUID.randomUUID().toString();
        return prefix + uniquePart;
    }

    public String UUIDForFinalOptimizedDetail() {
        String prefix = "FPTD";
        String uniquePart = UUID.randomUUID().toString();
        return prefix + uniquePart;
    }

    public String UUIDForFinalOptimized() {
        String prefix = "FPT";
        String uniquePart = UUID.randomUUID().toString();
        return prefix + uniquePart;
    }
}
