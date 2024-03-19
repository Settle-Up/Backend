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
        // UUID 생성 후 "-"를 제거하고 문자열에 추가
        String uniquePart = UUID.randomUUID().toString().replace("-", "");
        return prefix + uniquePart;
    }

    public String UUIDForOptimizedTransaction() {
        String prefix = "OPT";
        // UUID 생성 후 "-"를 제거하고 문자열에 추가
        String uniquePart = UUID.randomUUID().toString().replace("-", "");
        return prefix + uniquePart;
    }

    public String UUIDForOptimizedTransactionsDetail() {
        String prefix = "OPTD";
        // UUID 생성 후 "-"를 제거하고 문자열에 추가
        String uniquePart = UUID.randomUUID().toString().replace("-", "");
        return prefix + uniquePart;
    }
}
