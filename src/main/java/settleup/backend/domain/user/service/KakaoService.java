package settleup.backend.domain.user.service;

import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.entity.dto.LoginDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.domain.user.entity.dto.KakaoTokenDto;

public interface KakaoService {

    KakaoTokenDto getKakaoAccessToken(String code);

    UserInfoDto getUserInfo(String accessToken) throws CustomException;

    LoginDto registerUser(UserInfoDto userInfoDto) throws CustomException;



}