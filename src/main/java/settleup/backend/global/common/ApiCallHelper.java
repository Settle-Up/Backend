package settleup.backend.global.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
@AllArgsConstructor
public class ApiCallHelper {

    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    public <T> T callExternalApi(String uri, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) throws CustomException {
        ResponseEntity<String> response = restTemplate.exchange(uri, method, requestEntity, String.class);
        try {
            return objectMapper.readValue(response.getBody(), responseType);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.PARSE_ERROR);
        }
    }


    public <T> ResponseEntity<T> postExternalApi(String uri, HttpMethod method, HttpEntity<?> requestEntity, Class<T> responseType) throws CustomException {
        ResponseEntity<T> response = restTemplate.exchange(uri, method, requestEntity, responseType);

        return response;
    }

    public CompletableFuture<JsonNode> callExternalApiByAsync(String uri, HttpMethod method, HttpEntity<?> requestEntity) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                ResponseEntity<String> response = restTemplate.exchange(uri, method, requestEntity, String.class);
                return objectMapper.readTree(response.getBody());
            } catch (Exception e) {
                throw new CompletionException(new CustomException(ErrorCode.PARSE_ERROR));
            }
        });
    }


    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        return headers;
    }

}


