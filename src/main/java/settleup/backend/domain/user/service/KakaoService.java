package settleup.backend.domain.user.service;

import settleup.backend.global.api.KakaoTokenDto;

public interface KakaoService {
 void RequestToSocialValidCode();
 KakaoTokenDto getKakaoAccessToken(String code);
}
