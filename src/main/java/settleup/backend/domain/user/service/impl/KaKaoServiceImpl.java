package settleup.backend.domain.user.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import settleup.backend.domain.user.service.KakaoService;
import settleup.backend.global.api.KakaoTokenDto;
import settleup.backend.global.config.KakaoConfig;




@Service
@Transactional
@AllArgsConstructor
public class KaKaoServiceImpl implements KakaoService {

    private final KakaoConfig kakaoConfig;
    private final RestTemplate restTemplate;


    @Override
    public void RequestToSocialValidCode() {
        String kakaoAuthUrl = kakaoConfig.getAuthUri()+
                "?response_type=code" +
                "&client_id=" + kakaoConfig.getClientId() +
                "&redirect_uri=" + kakaoConfig.getRedirectUri();

        restTemplate.getForEntity(kakaoAuthUrl, String.class);
    }

        @Override
        public KakaoTokenDto getKakaoAccessToken(String code) {

            HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = getMapHttpEntity(code);

            // 카카오로부터 Access token 받아오기
            RestTemplate rt = new RestTemplate();
            ResponseEntity<String> accessTokenResponse = rt.exchange(
                    kakaoConfig.getTokenUri(),
                    HttpMethod.POST,
                    kakaoTokenRequest,
                    String.class
            );

            // JSON Parsing (-> KakaoTokenDto)
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            KakaoTokenDto kakaoTokenDto = null;
            try {
                kakaoTokenDto = objectMapper.readValue(accessTokenResponse.getBody(), KakaoTokenDto.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

            return kakaoTokenDto;
        }

    private HttpEntity<MultiValueMap<String, String>> getMapHttpEntity(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", kakaoConfig.getType()); //카카오 공식문서 기준 authorization_code 로 고정
        params.add("client_id", kakaoConfig.getClientId()); // 카카오 Dev 앱 REST API 키
        params.add("redirect_uri", kakaoConfig.getRedirectUri()); // 카카오 Dev redirect uri
        params.add("code", code); // 프론트에서 인가 코드 요청시 받은 인가 코드값
        params.add("client_secret", kakaoConfig.getSecret()); // 카카오 Dev 카카오 로그인 Client Secret

        // 헤더와 바디 합치기 위해 Http Entity 객체 생성
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);
        return kakaoTokenRequest;
    }
}

