package settleup.backend.domain.user.service.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.user.entity.DemoUserEntity;
import settleup.backend.domain.user.entity.dto.LoginDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.DemoUserRepository;
import settleup.backend.domain.user.service.DemoUserService;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.Helper.ApiCallHelper;
import settleup.backend.global.Helper.ResponseDto;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Helper.UUID_Helper;
import settleup.backend.global.Util.JwtProvider;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@Transactional
@AllArgsConstructor
public class DemoUserServiceImpl implements DemoUserService {

    private final JwtProvider provider;
    private final UUID_Helper uuidHelper;
    private final DemoUserRepository demoUserRepo;
    private final ApiCallHelper apiCallHelper;
    private final LoginService loginService;


    private static final Logger logger = LoggerFactory.getLogger(DemoUserServiceImpl.class);
    @Override
    public LoginDto createDemoToken(String userName ,String ip) throws CustomException {
        UserInfoDto demoUser = temporaryUserSave(userName,ip);
        LoginDto demoAccessInfo = demoToken(demoUser);
        return demoAccessInfo;
    }


    private LoginDto demoToken(UserInfoDto demoUserInfo) throws CustomException {
        try {
            LoginDto loginDto = new LoginDto();
            loginDto.setAccessToken(provider.createDemoUserToken(demoUserInfo));
            loginDto.setSubject("DemoLogin");
            loginDto.setExpiresIn("60 min");
            loginDto.setIssuedTime(new Date().toString());
            loginDto.setUserId(demoUserInfo.getUserId());
            loginDto.setUserName(demoUserInfo.getUserName());
            loginDto.setDemoUserTemporaryEmail(demoUserInfo.getUserEmail());
            return loginDto;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOKEN_CREATION_FAILED);
        }
    }
    private UserInfoDto temporaryUserSave(String userName ,String userIp) throws CustomException {
        logger.info("Starting temporaryUserSave method for userName: {}", userName);
        try {
            LocalDateTime now = LocalDateTime.now();
            UserInfoDto demoUserDto = new UserInfoDto();
            demoUserDto.setUserPhone(null);
            String email = uuidHelper.demoUserRandomEmail(userName);
            String userId = uuidHelper.demoUserUUID();
            demoUserDto.setUserEmail(email);
            demoUserDto.setUserId(userId);
            demoUserDto.setIsRegularUserOrDemoUser(false);
            demoUserDto.setUserName(userName);

            logger.debug("Created UserInfoDto: {}", demoUserDto);

            DemoUserEntity demoUserEntity = new DemoUserEntity();
            demoUserEntity.setUserUUID(demoUserDto.getUserId());
            demoUserEntity.setUserName(demoUserDto.getUserName());
            demoUserEntity.setUserEmail(email);
            demoUserEntity.setUserPhone(null);
            demoUserEntity.setIsDecimalInputOption(false);
            demoUserEntity.setCreatedAt(now);
            demoUserEntity.setUserType(Status.DEMO);
            demoUserEntity.setIp(userIp);
            demoUserEntity.setDummy(false);

            logger.debug("Created DemoUserEntity: {}", demoUserEntity);

            demoUserRepo.save(demoUserEntity);
            logger.info("Saved DemoUserEntity to database");

            return demoUserDto;
        } catch (Exception e) {
            logger.error("Error saving temporary user", e);
            throw new CustomException(ErrorCode.DATABASE_ERROR);
        } finally {
            logger.info("Finished temporaryUserSave method for userName: {}", userName);
        }
    }

    @Override
    public LoginDto retrieveDemoUserInfo(String token) throws CustomException {
        UserInfoDto userInfoDto= loginService.validTokenOrNot(token);
        try {
            LoginDto loginDto = new LoginDto();
            loginDto.setAccessToken(provider.removeBearerTokenPrefix(token));
            loginDto.setSubject("DemoLogin");
            loginDto.setExpiresIn("60 min");
            loginDto.setIssuedTime(new Date().toString());
            loginDto.setUserId(userInfoDto.getUserId());
            loginDto.setUserName(userInfoDto.getUserName());
            loginDto.setDemoUserTemporaryEmail(userInfoDto.getUserEmail());
            return loginDto;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TOKEN_CREATION_FAILED);
        }
    }
}
