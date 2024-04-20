package settleup.backend.global.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class KakaoConfig {
    @Value("${oauth.kakao.client_id}")
    private String clientId;

    @Value("${oauth.kakao.redirect_uri_dev}")
    private String redirectUriDev;

    @Value("${oauth.kakao.redirect_uri_prod}")
    private String redirectUriProd;

    @Value("${oauth.kakao.kakao_auth_uri}")
    private String authUri;

    @Value("${oauth.kakao.grant_type}")
    private String type;

    @Value("${oauth.kakao.client_secret}")
    private String secret;

    @Value("${oauth.kakao.kakao_token_uri}")
    private String tokenUri;

    @Value("${oauth.kakao.user_info_accept}")
    private String userInfoUri;
}
