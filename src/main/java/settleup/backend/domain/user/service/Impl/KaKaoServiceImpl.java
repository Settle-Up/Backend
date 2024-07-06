package settleup.backend.domain.user.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import settleup.backend.global.Helper.ApiCallHelper;
import settleup.backend.global.Helper.UUID_Helper;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.LoginDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.KakaoService;
import settleup.backend.global.Util.JwtProvider;
import settleup.backend.domain.user.entity.dto.KakaoTokenDto;
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
     * getKakaoAccessToken 인증번호로 카카오에 토큰요청
     *
     * @param code (validCode)
     * @throws CustomException EXTERNAL_API_ERROR_SOCIAL_TOKEN(113, "Failed to get social external api access token")
     * @process request to Kakao Post /response receive as KakaoTokenDto
     */

    @Override
    public KakaoTokenDto getKakaoAccessToken(String code) throws CustomException {
        try {
            HttpHeaders headers = apiCallHelper.createHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoConfig.getClientId());
            params.add("redirect_uri",kakaoConfig.getRedirectUriDev());
//            if (!isHttps()) {
//                params.add("redirect_uri", kakaoConfig.getRedirectUriDev());
//            } else {
//                params.add("redirect_uri", kakaoConfig.getRedirectUriProd());
//            }
            params.add("code", code);
            params.add("client_secret", kakaoConfig.getSecret());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            return apiCallHelper.callExternalApi(kakaoConfig.getTokenUri(), HttpMethod.POST, requestEntity, KakaoTokenDto.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR_SOCIAL_TOKEN);
        }
    }

//    private boolean isHttps() {
//        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
//        return "https".equals(request.getHeader("X-Forwarded-Proto"));
//    }


    /**
     * getUserInfo 카카오 토큰으로 카카오에서 유저 정보가져오기
     *
     * @param accessToken from Kakao
     * @return userInfoDto
     * @throws CustomException 1.EXTERNAL_API_EMPTY_RESPONSE (115, user info response is empty)
     *                         2.EXTERNAL_API_ERROR (114, Failed to retrieve user info from Kakao)
     * @process kakaoAccount = response (included flied kakao_account: {name, phone_number,email})
     * @privateMethod findUserInfoByKakao
     * @process kakaoAccount -> Transition (to) userInfoDto
     */
    @Override
    public UserInfoDto getUserInfo(String accessToken) throws CustomException {
        try {
            HttpHeaders headers = apiCallHelper.createHeaders();
            headers.setBearerAuth(accessToken);

            HttpEntity<?> requestEntity = new HttpEntity<>(headers);
            Map<String, Object> jsonResponse = apiCallHelper.callExternalApi(kakaoConfig.getUserInfoUri(), HttpMethod.GET, requestEntity, Map.class);
            System.out.println("hereisKaKaoResponse:" + jsonResponse);

            if (jsonResponse == null || jsonResponse.isEmpty()) {
                throw new CustomException(ErrorCode.EXTERNAL_API_EMPTY_RESPONSE);
            }
            Map<String, Object> kakaoAccount = (Map<String, Object>) jsonResponse.get("kakao_account");
            return findUserInfoByKakao(kakaoAccount);
        } catch (CustomException e) {
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
     * registerUser 우리사이트 토큰 발행
     *
     * @param userInfoDto {userName ,userName,userPhone }
     * @throws CustomException 1.REGISTRATION_FAILED (116,"Errors occurred during uuid generation or save db")
     *                         2.TOKEN_CREATION_FAILED (117,"Failed to create login token" )
     * @process <does user isPresent userEntity?>
     * -> true  then.set userInfoDto from userEntity
     * -> false then.1.create uuid from email 2. set userEntity form userInfoDto
     * @privateMethod createSettleUpLoginTokenInfo(userInfo)
     * @process set settleUpToKenDto using tokenProvider , userInfo
     */

    @Override
    public LoginDto registerUser(UserInfoDto userInfoDto) throws CustomException {
        try {
            Optional<UserEntity> existingUser = userRepo.findByUserEmail(userInfoDto.getUserEmail());
            if (existingUser.isPresent()) {
                UserInfoDto newUserInfoDto = new UserInfoDto();
                newUserInfoDto.setUserName(existingUser.get().getUserName());
                newUserInfoDto.setUserEmail(existingUser.get().getUserEmail());
                newUserInfoDto.setUserId(existingUser.get().getUserUUID());
                newUserInfoDto.setIsDecimalInputOption(existingUser.get().getIsDecimalInputOption());
                newUserInfoDto.setIsRegularUserOrDemoUser(true);
                return createSettleUpLoginInfo(newUserInfoDto);

            }
            String userUUID = uuidHelper.UUIDFromEmail(userInfoDto.getUserEmail());
            userInfoDto.setUserId(userUUID);

            UserEntity newUser = new UserEntity();
            newUser.setUserEmail(userInfoDto.getUserEmail());
            newUser.setUserName(userInfoDto.getUserName());
            newUser.setUserPhone(userInfoDto.getUserPhone());
            newUser.setUserUUID(userUUID);
            newUser.setIsDecimalInputOption(false);
            userRepo.save(newUser);
            return createSettleUpLoginInfo(userInfoDto);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.REGISTRATION_FAILED);
        }

    }

    private LoginDto createSettleUpLoginInfo(UserInfoDto userInfoDto) {
        try {
            LoginDto loginDto = new LoginDto();
            loginDto.setAccessToken(tokenProvider.createRegularUserToken(userInfoDto));
            loginDto.setSubject("FormalLogin");
            loginDto.setExpiresIn("1 day");
            loginDto.setIssuedTime(new Date().toString());
            loginDto.setUserId(userInfoDto.getUserId());
            loginDto.setUserName(userInfoDto.getUserName());
            return loginDto;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOKEN_CREATION_FAILED);
        }
    }

}
