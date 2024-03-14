package settleup.backend.domain.receipt.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.receipt.entity.dto.ReceiptRequestDto;
import settleup.backend.domain.receipt.entity.dto.RequireTransactionDto;
import settleup.backend.domain.receipt.receiptCommons.ControllerHelper;
import settleup.backend.domain.receipt.service.ReceiptService;
import settleup.backend.domain.transaction.service.RequireTransactionService;
import settleup.backend.domain.transaction.service.TransactionCoordinatorService;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;


@RestController
@AllArgsConstructor
@RequestMapping("/expense")
public class ReceiptController {


    private final LoginService loginService;
    private final TransactionCoordinatorService transactionCoordinatorService;


    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createExpenseByReceipt(
            @RequestHeader(value = "Authorization") String token, @RequestBody ReceiptRequestDto requestDto) throws CustomException {
        loginService.validTokenOrNot(token);
        String missingFields = ControllerHelper.checkRequiredWithFilter(requestDto);
        if (!missingFields.isEmpty()) {
            try {
                throw new Exception(missingFields);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        ResponseDto responseDto =transactionCoordinatorService.createExpenseByReceipt(requestDto);
        return ResponseEntity.ok(responseDto);
    }
}

