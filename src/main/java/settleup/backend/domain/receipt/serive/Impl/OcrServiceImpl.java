package settleup.backend.domain.receipt.serive.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
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


@Service
@Transactional
@AllArgsConstructor
public class OcrServiceImpl implements OcrService {

    private static final Logger logger = LoggerFactory.getLogger(OcrServiceImpl.class);
    private final ApiCallHelper apiCallHelper;
    private final AzureConfig azureConfig;
    private final AppConfig appConfig;

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



//    @Override
//    public HttpStatusCode postToAzureApi(String base64) {
//        try {
//            HttpHeaders headers = apiCallHelper.createHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON); // 예시로 JSON을 사용하는 경우
//            headers.set("Ocp-Apim-Subscription-Key", azureConfig.getApiKey());
//
//            Map<String, String> requestBody = new HashMap<>();
//            requestBody.put("base64Source", base64);
//
//            HttpEntity<?> requestEntity = new HttpEntity<>(requestBody, headers);
//            logger.debug("Sending request to Azure API: {}", azureConfig.getOcrUri());
//            logger.debug("Request body: {}", requestBody);
//
//           ResponseEntity< Map<String, Object>> responseEntity =(ResponseEntity)apiCallHelper.callExternalApi(azureConfig.getOcrUri(), HttpMethod.POST, requestEntity, Map.class);
//
////            if (jsonResponse == null || jsonResponse.isEmpty()) {
////                throw new CustomException(ErrorCode.EXTERNAL_API_EMPTY_RESPONSE);
////            }
//
//            if (responseEntity.getStatusCode() != HttpStatus.OK) {
//                throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
//            }
//
//            return responseEntity.getStatusCode();
//        } catch (Exception e) {
//            logger.error("Error during API call to Azure: ", e);
//            throw new RuntimeException(e);
//        }
//    }
//
    @Override
    public String postToAzureApi(String base64) {
        try {
            HttpHeaders headers = apiCallHelper.createHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Ocp-Apim-Subscription-Key", azureConfig.getApiKey());

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("base64Source", base64);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
            logger.debug("Sending request to Azure API: {}", azureConfig.getPostUrl());
            logger.debug("Request body: {}", requestBody);

            ResponseEntity<Map> responseEntity = apiCallHelper.postExternalApi(azureConfig.getPostUrl(), HttpMethod.POST, requestEntity, Map.class);

            HttpStatus status = (HttpStatus) responseEntity.getStatusCode();

            if (status == HttpStatus.ACCEPTED) {
                logger.debug("Request accepted with status 202, but no content.");
                String OperationLocation = responseEntity.getHeaders().getFirst("Operation-Location");
                return OperationLocation;
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
    }

    @Override
    public ResponseEntity<?> getToAzureApi(String operationLocation) {
        if (operationLocation == null) {
            throw new IllegalArgumentException("The operationLocation is null.");
        }

        HttpHeaders headers = apiCallHelper.createHeaders();
        headers.set("Ocp-Apim-Subscription-Key", azureConfig.getApiKey());

        HttpEntity<?> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<String> response = appConfig.restTemplate().exchange(
                operationLocation,
                HttpMethod.GET,
                requestEntity,
                String.class);

        return response;
    }
//@Override
//public CompletableFuture<String> postToAzureApi(String base64) {
//    HttpHeaders headers = apiCallHelper.createHeaders();
//    headers.setContentType(MediaType.APPLICATION_JSON);
//    headers.set("Ocp-Apim-Subscription-Key", azureConfig.getApiKey());
//
//    Map<String, String> requestBody = new HashMap<>();
//    requestBody.put("base64Source", base64);
//
//    HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);
//
//    return apiCallHelper.postExternalApi(azureConfig.getPostUrl(), HttpMethod.POST, requestEntity, Map.class)
//            .thenApply(responseEntity -> {
//                if (responseEntity.getStatusCode() == HttpStatus.ACCEPTED) {
//                    return responseEntity.getHeaders().getFirst("Operation-Location");
//                } else {
//                    throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
//                }
//            })
//            .thenCompose(this::getToAzureApi); // 이미 String을 반환하므로 추가 변환은 필요 없음
//}
//
//
//    @Override
//    public CompletableFuture<String> getToAzureApi(String operationLocation) {
//        return CompletableFuture.supplyAsync(() -> {
//            HttpHeaders headers = apiCallHelper.createHeaders();
//            headers.set("Ocp-Apim-Subscription-Key", azureConfig.getApiKey());
//
//            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
//            ResponseEntity<String> response = appConfig.restTemplate().exchange(
//                    operationLocation,
//                    HttpMethod.GET,
//                    requestEntity,
//                    String.class); // ResponseEntity<String>을 사용
//
//            return response.getBody(); // 바로 String 반환
//        });
//    }



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