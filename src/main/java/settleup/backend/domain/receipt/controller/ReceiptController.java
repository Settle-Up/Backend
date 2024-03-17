package settleup.backend.domain.receipt.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.domain.receipt.receiptCommons.ControllerHelper;
import settleup.backend.domain.receipt.service.ReceiptService;
import settleup.backend.domain.transaction.service.TransactionCoordinatorService;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;


@RestController
@AllArgsConstructor
@RequestMapping("")
public class ReceiptController {


    private final LoginService loginService;
    private final ReceiptService receiptService;
    private final TransactionCoordinatorService transactionCoordinatorService;


    @PostMapping("/expense/create")
    public ResponseEntity<ResponseDto> createExpenseByReceipt(
            @RequestHeader(value = "Authorization") String token, @RequestBody ReceiptDto requestDto) throws CustomException {
        loginService.validTokenOrNot(token);
        String missingFields = ControllerHelper.checkRequiredWithFilter(requestDto);
        if (!missingFields.isEmpty()) {
            try {
                throw new Exception(missingFields);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        ResponseDto responseDto = transactionCoordinatorService.createExpenseByReceipt(requestDto);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/group/detail")
    public ResponseEntity<ResponseDto> retrievedReceipt(
            @RequestHeader(value = "Authorization") String token,@RequestParam("receipt") String receiptUUID){
        loginService.validTokenOrNot(token);
        ReceiptDto data = receiptService.getReceiptInfo(receiptUUID);
        ResponseDto responseDto =new ResponseDto<>(true,"receipt detail retrieved successfully",data);
        return  ResponseEntity.ok(responseDto);

    }

}

