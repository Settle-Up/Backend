package settleup.backend.domain.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.user.entity.dto.FeedBackDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.EmailSenderService;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.domain.user.service.UserService;
import settleup.backend.global.Helper.ResponseDto;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {

    private LoginService loginService;
    private UserService userService;
    private EmailSenderService emailSenderService;

    @PostMapping("/profile")
    public ResponseEntity<ResponseDto> setDecimalOption(
            @RequestHeader(value = "Authorization") String token, @RequestBody UserInfoDto infoContainOptionValue) {
        UserInfoDto userInfo = loginService.validTokenOrNot(token);
        userInfo.setIsDecimalInputOption(infoContainOptionValue.getIsDecimalInputOption());
        Map<String, Object> data = userService.clusterUserDecimal(userInfo);
        ResponseDto responseDto =new ResponseDto<>(true,"User decimal input option status updated successfully",data);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/profile")
    public ResponseEntity<ResponseDto> retrievedDecimalOption(
            @RequestHeader(value = "Authorization") String token) {
        UserInfoDto userInfo = loginService.validTokenOrNot(token);
       Map<String,Object> data =  userService.retrievedUserDecimal(userInfo);
        ResponseDto responseDto =new ResponseDto<>(true,"User decimal input option retrieved successfully",data);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/feedback/email")
    public ResponseEntity<?> sendFeedbackEmail(@RequestBody FeedBackDto feedBackDto) {
        emailSenderService.sendFeedBackEmailToManager(feedBackDto);
        return ResponseEntity.ok("Feedback email sent successfully");
    }
}
