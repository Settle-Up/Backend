package settleup.backend.domain.receipt.service;

import com.fasterxml.jackson.databind.JsonNode;
import settleup.backend.domain.receipt.entity.dto.FormDataDto;
import settleup.backend.global.exception.CustomException;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;


    public interface OcrService {
        String processFormData(FormDataDto dataDto) throws IOException;

        CompletableFuture<String> postToAzureApi(String base64) throws CustomException;

        CompletableFuture<JsonNode>getToAzureApi(String operationLocation) throws CustomException;
    }
