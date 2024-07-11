package settleup.backend.domain.user.service.Impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.KakaoTokenDto;
import settleup.backend.domain.user.entity.dto.LoginDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.KaKaoProdService;
import settleup.backend.global.Helper.ApiCallHelper;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Helper.UUID_Helper;
import settleup.backend.global.Util.JwtProvider;
import settleup.backend.global.config.KakaoConfig;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.Date;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class KaKaoProdServiceImpl implements KaKaoProdService {

    private final KakaoConfig kakaoConfig;
    private final ApiCallHelper apiCallHelper;
    private final UserRepository userRepo;
    private final UUID_Helper uuidHelper;
    private final JwtProvider tokenProvider;

    private static final Logger logger = LoggerFactory.getLogger(KaKaoServiceImpl.class);


    @Override
    public KakaoTokenDto getKakaoAccessToken(String code) {
        try {
            HttpHeaders headers = apiCallHelper.createHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoConfig.getClientId());
            params.add("redirect_uri", kakaoConfig.getRedirectUriDev());
            params.add("code", code);
            params.add("client_secret", kakaoConfig.getSecret());

            HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

            return apiCallHelper.callExternalApi(kakaoConfig.getTokenUri(), HttpMethod.POST, requestEntity, KakaoTokenDto.class);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR_SOCIAL_TOKEN);
        }
    }


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
            Map<String, Object> properties = (Map<String, Object>) jsonResponse.get("properties");
            return findUserInfoByKakao(kakaoAccount, properties);
        } catch (CustomException e) {
            throw new CustomException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private UserInfoDto findUserInfoByKakao(Map<String, Object> kakaoAccount, Map<String, Object> properties) {
        UserInfoDto userInfoDto = new UserInfoDto();
        String nickname = (String) properties.get("nickname");
        String email = (String) kakaoAccount.get("email");

        userInfoDto.setUserName(nickname);
        userInfoDto.setUserEmail(email);
        return userInfoDto;
    }



    @Override
    public LoginDto registerUser(UserInfoDto userInfoDto) throws CustomException {
        try {
            logger.info("Starting registerUser method for userEmail: {}", userInfoDto.getUserEmail());

            Optional<UserEntity> existingUser = userRepo.findByUserEmail(userInfoDto.getUserEmail());
            if (existingUser.isPresent()) {
                logger.info("User already exists with email: {}", userInfoDto.getUserEmail());
                UserInfoDto newUserInfoDto = new UserInfoDto();
                newUserInfoDto.setUserName(existingUser.get().getUserName());
                newUserInfoDto.setUserEmail(existingUser.get().getUserEmail());
                newUserInfoDto.setUserId(existingUser.get().getUserUUID());
                newUserInfoDto.setIsDecimalInputOption(existingUser.get().getIsDecimalInputOption());
                newUserInfoDto.setIsRegularUserOrDemoUser(true);

                logger.info("Returning existing user information for userEmail: {}", userInfoDto.getUserEmail());
                return createSettleUpLoginInfo(newUserInfoDto);
            }

            String userUUID = uuidHelper.UUIDFromEmail(userInfoDto.getUserEmail());
            userInfoDto.setUserId(userUUID);
            userInfoDto.setIsRegularUserOrDemoUser(true);


            UserEntity newUser = new UserEntity();
            newUser.setUserEmail(userInfoDto.getUserEmail());
            newUser.setUserName(userInfoDto.getUserName());
            newUser.setUserUUID(userUUID);
            newUser.setIsDecimalInputOption(false);
            newUser.setUserType(Status.REGULAR);


            logger.debug("Creating new user with details: {}", newUser);

            userRepo.save(newUser);
            logger.info("New user created and saved successfully with email: {}", userInfoDto.getUserEmail());

            return createSettleUpLoginInfo(userInfoDto);
        } catch (Exception e) {
            logger.error("Error registering user with email: {}", userInfoDto.getUserEmail(), e);
            throw new CustomException(ErrorCode.REGISTRATION_FAILED);
        } finally {
            logger.info("Finished registerUser method for userEmail: {}", userInfoDto.getUserEmail());
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


//{
//        "id": 3617037595,
//        "connected_at": "2024-07-10T22:57:38Z",
//        "properties": {
//            "nickname": "서동희"
//        },
//        "kakao_account": {
//            "profile_nickname_needs_agreement": false,
//            "profile": {
//                "nickname": "서동희",
//                "is_default_nickname": false
//            },
//            "has_email": true,
//            "email_needs_agreement": false,
//            "is_email_valid": true,
//            "is_email_verified": true,
//            "email": "seodonghee456@gmail.com"
//        }
