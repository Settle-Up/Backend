package settleup.backend.domain.receipt.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.receipt.entity.dto.FormDataDto;
import settleup.backend.domain.receipt.serive.OcrService;

import java.io.IOException;

import org.springframework.web.context.request.async.DeferredResult;


@RestController
@AllArgsConstructor
@RequestMapping("/receipt")
public class OcrController {

    private final OcrService ocrService;
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);


    @PostMapping("azure/callback")
    public DeferredResult<ResponseEntity<?>> externalData(@ModelAttribute FormDataDto dataDto) {
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
    @PostMapping("/create")
    public ResponseEntity<?> createReceipt(@RequestBody JsonNode jsonNode) {
        //
        return ResponseEntity.ok().body(jsonNode);
    }
}

