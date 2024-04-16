package settleup.backend.domain.transaction.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateDto;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateRequestDto;
import settleup.backend.domain.transaction.service.TransactionUpdateService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.common.ResponseDto;
import settleup.backend.global.exception.CustomException;

@RestController
@AllArgsConstructor
public class TransactionManageController {
    private final LoginService loginService;
    private final TransactionUpdateService transactionUpdateService;

    @PatchMapping("/transaction/manage")
    public ResponseEntity<ResponseDto> updateTransaction(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam("groupId") String groupId,
            @RequestBody TransactionUpdateRequestDto requestDto) throws CustomException {
        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        TransactionUpdateDto data = transactionUpdateService.transactionManage(userInfoDto, groupId, requestDto);
        ResponseDto responseDto = new ResponseDto<>(true, "update retrieved successfully", data);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/notifications/transactions/received")
    public ResponseEntity<ResponseDto> retrievedUpdateData(@RequestHeader(value = "Authorization") String token) throws CustomException {
        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        TransactionUpdateDto data = transactionUpdateService.retrievedReceivedListInGroup(userInfoDto);
        ResponseDto responseDto = new ResponseDto<>(true, "update list retrieved successfully", data);
        return ResponseEntity.ok(responseDto);
    }
}
