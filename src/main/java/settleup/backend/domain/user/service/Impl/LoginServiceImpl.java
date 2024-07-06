package settleup.backend.domain.user.service.Impl;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.user.entity.DemoUserEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.LoginDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.DemoUserRepository;
import settleup.backend.global.Helper.UUID_Helper;
import settleup.backend.global.Util.ServerCryptUtil;
import settleup.backend.global.config.CryptographyConfig;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.Util.JwtProvider;

import java.util.Date;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final JwtProvider provider;
    private final UserRepository userRepo;
    private final DemoUserRepository demoUserRepo;
    private final UUID_Helper uuidHelper;
    private final CryptographyConfig cryptographyConfig;


    private static final Logger logger = LoggerFactory.getLogger(LoginServiceImpl.class);

    /**
     * validTokenOrNot
     *
     * @param token
     * @return
     * @throws CustomException
     * @process
     */

    @Override
    public UserInfoDto validTokenOrNot(String token) throws CustomException {
        logger.info("validTokenOrNot method called with token: {}", token);

        Claims claims = provider.parseJwtTokenGenerationInfo(token);
        logger.info("Token parsed successfully: {}", claims);

        if (!claims.getIssuer().equals("SettleUp")) {
            logger.error("Invalid token issuer: {}", claims.getIssuer());
            throw new CustomException(ErrorCode.TOKEN_WRONG_ISSUER);
        }

        String decryptedUserUUID = ServerCryptUtil.decrypt(claims.get("server-specified-key-01").toString(), cryptographyConfig.getEncryptionSecretKey());
        String decryptedUserName = ServerCryptUtil.decrypt(claims.get("server-specified-key-02").toString(), cryptographyConfig.getEncryptionSecretKey());
        logger.info("Decrypted user UUID: {}", decryptedUserUUID);
        logger.info("Decrypted user name: {}", decryptedUserName);

        Boolean isRegularUser = (Boolean) claims.get("userType");
        logger.info("User type is regular: {}", isRegularUser);

        UserInfoDto userInfoDto = new UserInfoDto();

        if (isRegularUser) {
            Optional<UserEntity> existingUser = userRepo.findByUserUUID(decryptedUserUUID);

            if (!existingUser.isPresent()) {
                logger.error("User not found with UUID: {}", decryptedUserUUID);
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }

            logger.info("User found: {}", existingUser.get());
            userInfoDto.setUserId(decryptedUserUUID);
            userInfoDto.setUserName(decryptedUserName);
            userInfoDto.setIsRegularUserOrDemoUser(true);
        } else {
            Optional<DemoUserEntity> existingUser = demoUserRepo.findByUserUUID(decryptedUserUUID);

            if (!existingUser.isPresent()) {
                logger.error("Demo user not found with UUID: {}", decryptedUserUUID);
                throw new CustomException(ErrorCode.USER_NOT_FOUND);
            }

            logger.info("Demo user found: {}", existingUser.get());
            userInfoDto.setUserId(decryptedUserUUID);
            userInfoDto.setUserName(decryptedUserName);
            userInfoDto.setIsRegularUserOrDemoUser(false);
        }

        logger.info("Returning UserInfoDto: {}", userInfoDto);
        return userInfoDto;
    }


    @Override
    public LoginDto createDemoToken(String userName) throws CustomException {
        UserInfoDto demoUser = temporaryUserSave(userName);
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

    private UserInfoDto temporaryUserSave(String userName) throws CustomException {
        logger.info("Starting temporaryUserSave method for userName: {}", userName);
        try {
            Date now = new Date();
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
}
