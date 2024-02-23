package settleup.backend.domain.user.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import settleup.backend.domain.user.service.KakaoService;
import settleup.backend.global.api.ResponseDto;


@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class UserController {

    private  final KakaoService kakaoService;


    @GetMapping("/kakao")
    public ResponseEntity<String> redirectAuthCodeRequestUrl() {
        kakaoService.RequestToSocialValidCode();
        return ResponseEntity.ok().build();
    }

    @GetMapping("/kakao/callback")
    public ResponseEntity<String> getTokenFromSocial(@RequestParam("code")String code){

        return ResponseEntity.ok().build();
    }
}


