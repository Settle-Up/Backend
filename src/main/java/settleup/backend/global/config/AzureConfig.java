package settleup.backend.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class AzureConfig {
    @Value("${azure.post_url}")
    private String postUrl;

    @Value("${azure.get_url}")
    private String getUrl;

    @Value("${azure.api_key}")
    private String apiKey;
}
