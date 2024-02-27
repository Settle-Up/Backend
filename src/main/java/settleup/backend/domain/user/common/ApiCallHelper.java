package settleup.backend.domain.user.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import settleup.backend.domain.user.exception.CustomException;
import settleup.backend.domain.user.exception.ErrorCode;

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

    public HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        return headers;
    }

}


