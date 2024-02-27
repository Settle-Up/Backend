package settleup.backend.domain.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.entity.dto.TokenDto;
import settleup.backend.domain.user.exception.CustomException;
import settleup.backend.domain.user.service.KakaoService;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.KakaoTokenDto;
import settleup.backend.global.api.ResponseDto;

import java.util.HashMap;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class UserController {

    private final KakaoService kakaoService;
    private final LoginService loginService;

    /**
     * 우리 서비스의 토큰이 없는 경우
     *
     * @param validCode
     * @return
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<ResponseDto> getTokenFromSocial(@RequestParam("code") String validCode) {
        try {
            KakaoTokenDto tokenInfo = kakaoService.getKakaoAccessToken(validCode);
            UserInfoDto userInfo = kakaoService.getUserInfo(tokenInfo.getAccess_token());
            TokenDto settleUpLogin = kakaoService.registerUser(userInfo);
            ResponseDto responseDto = new ResponseDto(true, "successfully login", settleUpLogin);
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (CustomException e) {
            ResponseDto<Void> responseDto = new ResponseDto<>(false, e.getErrorCodeName(), null, e.getSimpleErrorCode());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        }
    }

    /**
     * 우리 서비스의 토큰이 있는 경우
     * @param token
     * @return userUUID ,userName
     * @error 유효하지 않은 토큰
     */
    @GetMapping("/checkToken")
    public ResponseEntity<ResponseDto> checkToken(@RequestHeader(value = "Authorization") String token) {
        try {
            UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userUUID", userInfoDto.getUserUUID());
            userInfo.put("userName", userInfoDto.getUserName());

            ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>(true, "Token is valid, Login success", userInfo, null);
            return ResponseEntity.status(HttpStatus.OK).body(responseDto);
        } catch (CustomException e) {
            ResponseDto<Void> responseDto = new ResponseDto<>(false, e.getErrorCodeName(), null, e.getSimpleErrorCode());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
        }
    }
}




