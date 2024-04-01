package settleup.backend.domain.transaction.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateDto;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateRequestDto;
import settleup.backend.domain.transaction.service.TransactionUpdateService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;

import java.util.concurrent.ExecutionException;

@RestController
@AllArgsConstructor
@RequestMapping("/transaction")
public class TransactionUpdateController {
    private final LoginService loginService;
    private final TransactionUpdateService transactionUpdateService;

    @PatchMapping("/approval")
    public ResponseEntity<ResponseDto> updateTransaction(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam("groupId") String groupId,
            @RequestBody TransactionUpdateRequestDto requestDto) throws CustomException{
        UserInfoDto userInfoDto =loginService.validTokenOrNot(token);
        TransactionUpdateDto data = transactionUpdateService.transactionUpdate(userInfoDto, groupId, requestDto);
        ResponseDto responseDto = new ResponseDto<>(true, "update retrieved successfully", data);
        return ResponseEntity.ok(responseDto);
    }
    @GetMapping("/history")
    public ResponseEntity<ResponseDto> retrievedUpdateData(@RequestHeader(value = "Authorization")String token) throws  CustomException{
        UserInfoDto userInfoDto =loginService.validTokenOrNot(token);
        TransactionUpdateDto data=transactionUpdateService.retrievedUpdateListInGroup(userInfoDto);
        ResponseDto responseDto = new ResponseDto<>(true,"update list retrieved successfully",data);
        return ResponseEntity.ok(responseDto);
    }

}
