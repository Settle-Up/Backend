package settleup.backend.domain.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.domain.user.service.UserService;
import settleup.backend.global.common.ResponseDto;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private LoginService loginService;
    private UserService userService;

    @PostMapping("/option/decimal")
    public ResponseEntity<ResponseDto> setDecimalOption(
            @RequestHeader(value = "Authorization") String token, @RequestBody UserInfoDto infoContainOptionValue) {
        UserInfoDto userInfo = loginService.validTokenOrNot(token);
        userInfo.setIsDecimalInputOption(infoContainOptionValue.getIsDecimalInputOption());
        Map<String, String> data = userService.clusterUserDecimal(userInfo);
        ResponseDto responseDto =new ResponseDto<>(true,"User decimal input option status updated successfully",data);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/option/decimal")
    public ResponseEntity<ResponseDto> retrievedDecimalOption(
            @RequestHeader(value = "Authorization") String token) {
        UserInfoDto userInfo = loginService.validTokenOrNot(token);
       Map<String,String> data =  userService.retrievedUserDecimal(userInfo);
        ResponseDto responseDto =new ResponseDto<>(true,"User decimal input option retrieved successfully",data);
        return ResponseEntity.ok(responseDto);
    }
}
