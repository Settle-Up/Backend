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
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@RestController
@AllArgsConstructor
public class ReceiptController {

    private final LoginService loginService;
    private final ReceiptService receiptService;


    @PostMapping("/expense/create")
    public ResponseEntity<ResponseDto> createExpenseByReceipt(
            @RequestHeader(value = "Authorization") String token, @RequestBody ReceiptDto requestDto) {

            loginService.validTokenOrNot(token);
            String missingFields = ControllerHelper.checkRequiredWithFilter(requestDto);
            if (!missingFields.isEmpty()) {
                throw new CustomException(ErrorCode.INVALID_INPUT, "Missing fields: " + missingFields);
            }

            TransactionDto transactionDto = receiptService.createReceipt(requestDto);

            Map<String, Object> receiptInfo = new HashMap<>();
            receiptInfo.put("receiptId", transactionDto.getReceipt().getReceiptUUID());
            receiptInfo.put("receiptName", transactionDto.getReceipt().getReceiptName());
            receiptInfo.put("createdAt", transactionDto.getReceipt().getCreatedAt().toString());

            return ResponseEntity.ok(new ResponseDto(true, "Receipt creation process started.", receiptInfo));

    }


    @GetMapping("/group/detail")
    public ResponseEntity<ResponseDto> retrievedReceipt(
            @RequestHeader(value = "Authorization") String token, @RequestParam("receipt") String receiptUUID) {
        UserInfoDto userInfoDto= loginService.validTokenOrNot(token);
        ReceiptDto data = receiptService.getReceiptInfo(userInfoDto,receiptUUID);
        ResponseDto responseDto = new ResponseDto<>(true, "receipt detail retrieved successfully", data);
        return ResponseEntity.ok(responseDto);

    }

}

