package settleup.backend.domain.user.controller;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.Util.JwtProvider;
import settleup.backend.global.Util.RedisUtils;
import settleup.backend.global.Helper.BlackListInfo;
import settleup.backend.global.Helper.ResponseDto;

@RestController
@AllArgsConstructor
public class LogoutController {
    private LoginService loginService;
    private JwtProvider provider;
    private RedisUtils redisUtils;


    @PostMapping("/auth/logout")
    public ResponseEntity<ResponseDto> logout(@RequestHeader(value = "Authorization") String token) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        Claims claims = provider.parseJwtTokenGenerationInfo(token);
        long time = claims.getExpiration().getTime() - System.currentTimeMillis();
        BlackListInfo blackListInfo = new BlackListInfo(userInfoDto.getUserId(), userInfoDto.getUserName());

        redisUtils.setBlackList(token, blackListInfo, time);

        ResponseDto responseDto = new ResponseDto<>(true, "logout success", blackListInfo);
        return ResponseEntity.ok(responseDto);
    }
}
