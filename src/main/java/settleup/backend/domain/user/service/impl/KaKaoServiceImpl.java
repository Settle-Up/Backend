package settleup.backend.domain.user.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import settleup.backend.domain.user.common.ApiCallHelper;
import settleup.backend.domain.user.common.UUID_Helper;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.TokenDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.exception.CustomException;
import settleup.backend.domain.user.exception.ErrorCode;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.KakaoService;
import settleup.backend.global.Util.JwtProvider;
import settleup.backend.global.api.KakaoTokenDto;
import settleup.backend.global.config.KakaoConfig;
import java.util.Date;
import java.util.Map;
import java.util.Optional;


@Service
@Transactional
@AllArgsConstructor
public class KaKaoServiceImpl implements KakaoService {

    private final KakaoConfig kakaoConfig;
    private final ApiCallHelper apiCallHelper;
    private final UserRepository userRepo;
    private final UUID_Helper uuidHelper;
    private final JwtProvider tokenProvider;


    /**
     * 인증번호로 카카오에 토큰요청
     * @param code
     * @return 유저 정보 userInfo {userName, userPhone , userEmail}
     * @throws CustomException
     */

    @Override
    public KakaoTokenDto getKakaoAccessToken(String code) throws CustomException {
        try {
            HttpHeaders headers = apiCallHelper.createHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoConfig.getClientId());
            params.add("redirect_uri", kakaoConfig.getRedirectUri());
            params.add("code", code);
            params.add("client_secret", kakaoConfig.getSecret());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);


            return apiCallHelper.callExternalApi(kakaoConfig.getTokenUri(), HttpMethod.POST, requestEntity, KakaoTokenDto.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR_TOKEN);
        }
    }


    /**
     * 토큰으로 카카오에서 유저 정보가져오기
     * @param accessToken
     * @return 유저 정보 userInfo {userName, userPhone , userEmail}
     * @throws CustomException
     * @Method findUserInfoByKakao
     */
    @Override
    public UserInfoDto getUserInfo(String accessToken) throws CustomException {
        try {
            HttpHeaders headers = apiCallHelper.createHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            Map<String, Object> jsonResponse = apiCallHelper.callExternalApi(kakaoConfig.getUserInfoUri(), HttpMethod.GET, requestEntity, Map.class);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                throw new CustomException(ErrorCode.EXTERNAL_API_EMPTY_RESPONSE);
            }

            Map<String, Object> kakaoAccount = (Map<String, Object>) jsonResponse.get("kakao_account");
            return findUserInfoByKakao(kakaoAccount);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private UserInfoDto findUserInfoByKakao(Map<String, Object> kakaoAccount) {
        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setUserName((String) kakaoAccount.get("name"));
        userInfoDto.setUserPhone((String) kakaoAccount.get("phone_number"));
        userInfoDto.setUserEmail((String) kakaoAccount.get("email"));
        return userInfoDto;

    }

    /**
     * 우리사이트 토큰 발행
     * @param userInfoDto 카카오에서 받아서 정제한 우리 사이트 회원정보
     * @return
     * @throws CustomException
     */

    @Override
    public TokenDto registerUser(UserInfoDto userInfoDto) throws CustomException {
        try {
            Optional<UserEntity> existingUser = userRepo.findByUserEmail(userInfoDto.getUserEmail());
            if (existingUser.isPresent()) {
                UserInfoDto newUserInfoDto = new UserInfoDto();
                newUserInfoDto.setUserName(existingUser.get().getUserName());
                newUserInfoDto.setUserEmail(existingUser.get().getUserEmail());
                newUserInfoDto.setUserUUID(existingUser.get().getUserUUID());
                return createLoginInfo(newUserInfoDto);

            }
            String userUUID = uuidHelper.UUIDFromEmail(userInfoDto.getUserEmail());
            userInfoDto.setUserUUID(userUUID);

            UserEntity newUser = new UserEntity();
            newUser.setUserEmail(userInfoDto.getUserEmail());
            newUser.setUserName(userInfoDto.getUserName());
            newUser.setUserPhone(userInfoDto.getUserPhone());
            newUser.setUserUUID(userUUID);
            userRepo.save(newUser);
            return createLoginInfo(userInfoDto);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REGISTRATION_FAILED);
        }

    }

    private TokenDto createLoginInfo(UserInfoDto userInfoDto) {
        try {
            TokenDto tokenDto = new TokenDto();
            tokenDto.setAccessToken(tokenProvider.createToken(userInfoDto));
            tokenDto.setSubject("ForSettleUpLogin");
            tokenDto.setExpiresIn("1 day");
            tokenDto.setIssuedTime(new Date().toString());
            tokenDto.setUserName(userInfoDto.getUserName());
            tokenDto.setUserUUID(userInfoDto.getUserUUID());
            return tokenDto;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOKEN_CREATION_FAILED);
        }
    }

}
