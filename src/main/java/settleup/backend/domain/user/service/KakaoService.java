package settleup.backend.domain.user.service;

import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.entity.dto.TokenDto;
import settleup.backend.domain.user.exception.CustomException;
import settleup.backend.global.api.KakaoTokenDto;

public interface KakaoService {

 KakaoTokenDto getKakaoAccessToken(String code);
 UserInfoDto getUserInfo(String accessToken) throws CustomException;

 TokenDto registerUser(UserInfoDto userInfoDto) throws CustomException;


}
