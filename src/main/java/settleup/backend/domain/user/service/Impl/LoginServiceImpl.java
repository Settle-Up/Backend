package settleup.backend.domain.user.service.Impl;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.Util.ServerCryptUtil;
import settleup.backend.global.config.CryptographyConfig;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.Util.JwtProvider;

import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final JwtProvider provider;
    private final UserRepository userRepo;
    private final CryptographyConfig cryptographyConfig;


    /**
     * validTokenOrNot
     * @param token
     * @process
     * @return
     * @throws CustomException
     */

    @Override
    public UserInfoDto validTokenOrNot(String token) throws CustomException {

        Claims claims = provider.parseJwtTokenGenerationInfo(token);

        if (!claims.getSubject().equals("ForSettleUpLogin")) {
            throw new CustomException(ErrorCode.TOKEN_WRONG_SUBJECT);
        }

        String decryptedUserUUID = ServerCryptUtil.decrypt(claims.get("server-specified-key-01").toString(),cryptographyConfig.getEncryptionSecretKey());
        String decryptedUserName = ServerCryptUtil.decrypt(claims.get("server-specified-key-02").toString(),cryptographyConfig.getEncryptionSecretKey());
        Optional<UserEntity> existingUser = userRepo.findByUserUUID(decryptedUserUUID);

        if (!existingUser.isPresent()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setUserId(decryptedUserUUID);
        userInfoDto.setUserName(decryptedUserName);
        return userInfoDto;
    }
}

