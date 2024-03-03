package settleup.backend.domain.receipt.serive.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;
import settleup.backend.domain.receipt.entity.dto.FormDataDto;
import settleup.backend.domain.receipt.serive.OcrService;
import settleup.backend.global.common.ApiCallHelper;
import settleup.backend.global.config.AppConfig;
import settleup.backend.global.config.AzureConfig;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@Service
@Transactional
@AllArgsConstructor
public class OcrServiceImpl implements OcrService {

    private static final Logger logger = LoggerFactory.getLogger(OcrServiceImpl.class);
    private final ApiCallHelper apiCallHelper;
    private final AzureConfig azureConfig;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public String processFormData(FormDataDto dataDto) throws IOException {
        MultipartFile file = dataDto.getImage();
        try {
            String base64Image = convertToBase64(file);
            logger.info("Image converted to base64: {}", base64Image);
            return base64Image;
        } catch (IOException e) {
            logger.error("Failed to convert file to base64: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public CompletableFuture<String> postToAzureApi(String base64) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                HttpHeaders headers = apiCallHelper.createHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                headers.set("Ocp-Apim-Subscription-Key", azureConfig.getApiKey());

                Map<String, String> requestBody = new HashMap<>();
                requestBody.put("base64Source", base64);
                HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
                logger.debug("Sending request to Azure API: {}", azureConfig.getPostUrl());
                logger.debug("Request body: {}", requestBody);

                ResponseEntity<Map> responseEntity = apiCallHelper.postExternalApi(
                        azureConfig.getPostUrl(),
                        HttpMethod.POST,
                        requestEntity,
                        Map.class);

                HttpStatus status = (HttpStatus) responseEntity.getStatusCode();

                if (status == HttpStatus.ACCEPTED) {
                    logger.debug("Request accepted with status 202, but no content.");
                    return responseEntity.getHeaders().getFirst("Operation-Location");
                } else {
                    logger.error("API call failed or returned unexpected status: {}", status);
                    throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
                }
            } catch (HttpClientErrorException e) {
                logger.error("Client error during API call to Azure: ", e);
                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
            } catch (Exception e) {
                logger.error("Error during API call to Azure: ", e);
                throw new RuntimeException("Error during API call to Azure", e);
            }
        });
    }



    @Async
    public CompletableFuture<JsonNode> getToAzureApi(String operationLocation) {
        CompletableFuture<JsonNode> resultFuture = new CompletableFuture<>();

        checkStatus(operationLocation, resultFuture);

        return resultFuture;
    }

    private void checkStatus(String operationLocation, CompletableFuture<JsonNode> resultFuture) {
        scheduler.schedule(() -> {
            HttpHeaders headers = apiCallHelper.createHeaders();
            headers.set("Ocp-Apim-Subscription-Key", azureConfig.getApiKey());
            logger.debug("Initiating status check for operationLocation: {}", operationLocation);

            HttpEntity<?> requestEntity = new HttpEntity<>(headers);

            // 비동기 API 호출하여 JsonNode 반환
            CompletableFuture<JsonNode> responseFuture = apiCallHelper.callExternalApiByAsync(operationLocation, HttpMethod.GET, requestEntity);

            // 비동기 응답 처리
            responseFuture.thenAccept(jsonNode -> {
                String status = jsonNode.path("status").asText();
                logger.debug("Received status: {} for operationLocation: {}", status, operationLocation);

                if ("succeeded".equals(status)) {
                    // status가 succeeded 일 때 전체 응답 본문을 CompletableFuture에 설정
                    logger.info("Operation succeeded for operationLocation: {}", operationLocation);
                    resultFuture.complete(jsonNode);
                } else if ("running".equals(status)) {
                    // 상태가 running이면 다시 상태 확인
                    logger.debug("Operation still running, scheduling another check for operationLocation: {}", operationLocation);
                    checkStatus(operationLocation, resultFuture);
                } else {
                    // 실패나 예상치 못한 상태 처리
                    logger.error("Failed or unexpected status: {} for operationLocation: {}", status, operationLocation);
                    resultFuture.completeExceptionally(new RuntimeException("Failed or unexpected status: " + status));
                }
            }).exceptionally(ex -> {
                logger.error("Exception occurred while processing the response for operationLocation: {}", operationLocation, ex);
                resultFuture.completeExceptionally(ex);
                return null;
            });
        }, 5, TimeUnit.SECONDS); // 상태를 다시 확인하기 전에 대기할 시간. 필요에 따라 조정 가능
    }

    private String convertToBase64(MultipartFile file) throws IOException {
        try {
            byte[] fileContent = file.getBytes();
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            logger.error("Failed to read file content: {}", e.getMessage());
            throw e;
        }
    }
}