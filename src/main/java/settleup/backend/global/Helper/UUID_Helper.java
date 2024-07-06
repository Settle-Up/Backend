package settleup.backend.global.Helper;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.UUID;

@Component
public class UUID_Helper {
    public String UUIDFromEmail(String email) {
        return String.valueOf(UUID.nameUUIDFromBytes(email.getBytes(StandardCharsets.UTF_8)));
    }

    public String demoUserUUID() {
        String prefix = "DemoUser";
        String uniquePart = UUID.randomUUID().toString();
        return prefix + uniquePart;
    }

    // 같은 이름으로 23,400 같은 거 가능
    public  String demoUserRandomEmail(String userName) {
        String uniquePart = getRandomThreeDigitNumber() + getRandomLowerCaseLetter();
        return userName + uniquePart + "@RandomId.settleUp";
    }

    // 3자리 랜덤 숫자를 생성하는 메서드
    private String getRandomThreeDigitNumber() {
        Random random = new Random();
        int number = random.nextInt(900) + 100; // 100 ~ 999 범위의 숫자 생성
        return String.valueOf(number);
    }

    // 랜덤 소문자 알파벳 하나를 생성하는 메서드
    private  char getRandomLowerCaseLetter() {
        Random random = new Random();
        return (char) (random.nextInt(26) + 'a'); // 'a' ~ 'z' 범위의 알파벳 생성
    }
        public String demoGroupUUID() {
        String prefix = "DemoGroup";
        String uniquePart = UUID.randomUUID().toString();
        return prefix + uniquePart;
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
