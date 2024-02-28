package settleup.backend.global.Util;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UrlProvider {
    private static final String BASE_URL = "https://settleUp.com/";
    public String generateUniqueUrl() {
        UUID uuid = UUID.randomUUID();
        return BASE_URL + uuid.toString();
    }
}