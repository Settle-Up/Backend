package settleup.backend.domain.receipt.serive;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import settleup.backend.domain.receipt.entity.dto.FormDataDto;
import settleup.backend.domain.receipt.entity.dto.OcrResponseDto;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


    public interface OcrService {
        String processFormData(FormDataDto dataDto) throws IOException;

        String postToAzureApi(String base64);

        ResponseEntity<?> getToAzureApi(String operationLocation);
    }
