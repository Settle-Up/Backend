package settleup.backend.domain.receipt.controller;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.receipt.entity.dto.FormDataDto;
import settleup.backend.domain.receipt.serive.OcrService;

import java.io.IOException;


@RestController
@AllArgsConstructor
@RequestMapping("/receipt")
public class OcrController {

    private final OcrService ocrService;
    private static final Logger logger = LoggerFactory.getLogger(OcrController.class);


        @PostMapping("azure/callback")
    public ResponseEntity<?> externalData(@ModelAttribute FormDataDto dataDto) {
        try {
            String base64 = ocrService.processFormData(dataDto);
            String ocrPostRequest= ocrService.postToAzureApi(base64);
            ResponseEntity<?> response =ocrService.getToAzureApi(ocrPostRequest);
            return response;
        } catch (IOException e) {
            logger.error("파일 인코딩 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("파일 처리 중 오류 발생");
        }
    }
}





