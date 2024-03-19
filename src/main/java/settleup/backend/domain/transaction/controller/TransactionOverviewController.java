package settleup.backend.domain.transaction.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import settleup.backend.domain.transaction.service.OptimizedService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.ResponseDto;

@RestController
@AllArgsConstructor
@RequestMapping("")
public class TransactionOverviewController {
    private final LoginService loginService;
    private final OptimizedService optimizedService;

//    @GetMapping("group/list/detail")
//    public ResponseEntity<ResponseDto> retrievedTransactionOverview(
//            @RequestHeader(value = "Authorization") String token ,@RequestParam("groupId") String groupUUID){
//    UserInfoDto userInfo=loginService.validTokenOrNot(token);
//    OptimizationTargetDto targetDto = new OptimizationTargetDto();
//    targetDto.setUserUUID(userInfo.getUserId());
//    targetDto.setGroupUUID(groupUUID);
//    optimizedService.optimizationOfPersonal(targetDto);
//    return null;
//    }

}
