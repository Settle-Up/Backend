package settleup.backend.domain.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.entity.dto.LoginDto;
import settleup.backend.domain.user.service.DemoUserService;
import settleup.backend.domain.user.service.KaKaoProdService;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.domain.user.entity.dto.KakaoTokenDto;
import settleup.backend.global.Helper.ResponseDto;
import settleup.backend.global.Util.RateLimitingProvider;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {


    private final LoginService loginService;
    private final KaKaoProdService kaKaoProdService;
    private final RateLimitingProvider rateLimitingProvider;
    private final DemoUserService demoUserService;


    @GetMapping("/login/social/kakao")
    public ResponseEntity<ResponseDto> getTokenFromSocial(@RequestParam("code") String validCode) {
        KakaoTokenDto tokenInfo = kaKaoProdService.getKakaoAccessToken(validCode);
        UserInfoDto userInfo = kaKaoProdService.getUserInfo(tokenInfo.getAccess_token());
        LoginDto loginDto = kaKaoProdService.registerUser(userInfo);
        ResponseDto responseDto = new ResponseDto(true, "successfully login", loginDto);
        return ResponseEntity.ok(responseDto);
    }

    /**
     * Demo user 에게 선택지 제공
     * c1) 로그 아웃을 안했다는 전제하에 토큰이 있다면 그 전에 만든 페이지로 리다이렉트 해주기
     * c2) 토큰이 없고 해당 ip 로 아이디를 만든지 60분이 지났다면 새로운 아이디 만들기
     * <p>
     * TODO //
     * 1. demo user ip 를 받아야 함
     * 2. 만약에 header에 토큰이 있다면 받아야함
     * 단 . 없는 경우도 성공 케이스지만
     * 필수조건 > 경우 해당 ip 가 60 이내로 아이디를 생성했다면 RateLimiting
     * 3. 토큰이 있고 , 그 토큰에 매핑되어 있는 ip 로 60 분이내 재 요청시 -> 토큰을 포함하여 http get /groups 로 전환
     *
     * @param userInfo
     * @return
     */

    @PostMapping("/demo-user")
    public ResponseEntity<ResponseDto> registerForTemporaryUser(@RequestBody UserInfoDto userInfo, HttpServletRequest request) {
        String userIp = getClientIp(request);
        String token = request.getHeader("Authorization");


        // rateLimiting 검사 통과
        // token 이 있다면 => 그 계정으로 /groups 로 리턴해줌
        // token 이 없다면 => 바로 새로 계정 생성
        //{"success":false,"message":"TOKEN EXPIRED","data":null,"errorCode":"109"}
        // 만약에 토큰이 만료 되었다면 그전 유저를 불러오는 방식이 아니라 (retrievedDemoINfo)를 쓰는 게 아니라
        // 새로운 유저 등록하는 것과 같이 rataLimiting rjatkdb crateDemoToken 로직을 탈수 있도록 해줘

        try {
            if (token != null) {
                UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
                if (userInfoDto.getIsRegularUserOrDemoUser() != null) {
                    LoginDto loginDto=demoUserService.retrieveDemoUserInfo(token);
                    ResponseDto responseDto = new ResponseDto(true, "successfully login", loginDto);
                    return ResponseEntity.ok(responseDto);
                }
            }
        } catch (CustomException e) {
            if (e.getErrorCode() != ErrorCode.TOKEN_EXPIRED) {
                throw e;
            }
        }

        rateLimitingProvider.checkRateLimit(userIp);

        LoginDto loginDto = demoUserService.createDemoToken(userInfo.getUserName(), userIp);
        ResponseDto responseDto = new ResponseDto(true, "successfully login", loginDto);
        return ResponseEntity.ok(responseDto);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }


//    @PostMapping("/demo-user")
//    public ResponseEntity<ResponseDto> registerForTemporaryUser(@RequestBody UserInfoDto userInfo) {
//        InputHelper.isValidInputDemoForUser(userInfo);
//        LoginDto loginDto = loginService.createDemoToken(userInfo.getUserName());
//        ResponseDto responseDto = new ResponseDto(true, "successfully login", loginDto);
//        return ResponseEntity.ok(responseDto);
//    }


    @GetMapping("/checkToken")
    public ResponseEntity<ResponseDto> checkToken(@RequestHeader(value = "Authorization") String token) {
        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", userInfoDto.getUserId());
        userInfo.put("userName", userInfoDto.getUserName());
        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>(true, "Token is valid, Login success", userInfo, null);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/check/connection")
    public ResponseEntity<String> getCheckConnection() {
        return ResponseEntity.ok("Server is up and running!");
    }

}




