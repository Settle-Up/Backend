package settleup.backend.domain.receipt.controller;

import io.sentry.Sentry;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.domain.receipt.receiptCommons.ControllerHelper;
import settleup.backend.domain.receipt.service.ReceiptService;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.service.TransactionSagaService;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@RestController
@AllArgsConstructor
@RequestMapping("")
public class ReceiptController {


    private final LoginService loginService;
    private final ReceiptService receiptService;
    private final TransactionSagaService transactionSagaService;


    @PostMapping("/expense/create")
    public ResponseEntity<ResponseDto> createExpenseByReceipt(
            @RequestHeader(value = "Authorization") String token, @RequestBody ReceiptDto requestDto) {
        try {
            loginService.validTokenOrNot(token);
            String missingFields = ControllerHelper.checkRequiredWithFilter(requestDto);
            if (!missingFields.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "Missing fields: " + missingFields);
            }

            TransactionDto transactionDto = receiptService.createReceipt(requestDto);

            CompletableFuture.runAsync(() ->
                            transactionSagaService.performOptimizationOperations(transactionDto))
                    .exceptionally(ex -> {
                        Sentry.captureException(ex);
                        return null;
                    });

            Map<String, Object> receiptInfo = new HashMap<>();
            receiptInfo.put("receiptId", transactionDto.getReceipt().getReceiptUUID());
            receiptInfo.put("receiptName", transactionDto.getReceipt().getReceiptName());
            receiptInfo.put("createdAt", transactionDto.getReceipt().getCreatedAt().toString());

            return ResponseEntity.ok(new ResponseDto(true, "Receipt creation process started.", receiptInfo));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseDto(false, "Error occurred", null));

        }
    }


    @GetMapping("/group/detail")
    public ResponseEntity<ResponseDto> retrievedReceipt(
            @RequestHeader(value = "Authorization") String token, @RequestParam("receipt") String receiptUUID) {
        loginService.validTokenOrNot(token);
        ReceiptDto data = receiptService.getReceiptInfo(receiptUUID);
        ResponseDto responseDto = new ResponseDto<>(true, "receipt detail retrieved successfully", data);
        return ResponseEntity.ok(responseDto);

    }

}

