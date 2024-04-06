package settleup.backend.domain.receipt.controller;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.receipt.entity.dto.FormDataDto;
import settleup.backend.domain.receipt.service.OcrService;

import java.io.IOException;

import org.springframework.web.context.request.async.DeferredResult;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.exception.CustomException;


@RestController
@AllArgsConstructor
@RequestMapping("/expense")
public class OcrController {

    private final OcrService ocrService;
    private final LoginService loginService;
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);


    @PostMapping("azure/callback")
    public DeferredResult<ResponseEntity<?>> externalData(
            @RequestHeader(value = "Authorization") String token, @ModelAttribute FormDataDto dataDto) throws CustomException {
        loginService.validTokenOrNot(token);
        DeferredResult<ResponseEntity<?>> deferredResult = new DeferredResult<>();
        logger.debug("Processing externalData request");

        try {
            String base64 = ocrService.processFormData(dataDto);
            logger.debug("Encoded file to base64");

            ocrService.postToAzureApi(base64)
                    .thenCompose(ocrService::getToAzureApi)
                    .thenAccept(result -> {
                        logger.debug("Received success response from getToAzureApi");
                        deferredResult.setResult(ResponseEntity.ok(result));
                    })
                    .exceptionally(ex -> {
                        logger.error("Exception occurred during async processing", ex);
                        deferredResult.setErrorResult(
                                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error during async")
                        );
                        return null;
                    });
        } catch (IOException e) {
            logger.error("error during file encodeing", e);
            deferredResult.setErrorResult(
                    ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("error during file processing")
            );
        }

        return deferredResult;
    }

}

