package settleup.backend.domain.user.service.impl;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.exception.CustomException;
import settleup.backend.domain.user.exception.ErrorCode;
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

    @Override
    public UserInfoDto validTokenOrNot(String token) throws CustomException {

        Claims claims = provider.parseJwtToken(token);

        if (!claims.getSubject().equals("ForSettleUpLogin")) {
            throw new CustomException(ErrorCode.TOKEN_WRONG_SUBJECT);
        }

        String userUUID = claims.get("userUUID", String.class);
        Optional<UserEntity> existingUser = userRepo.findByUserUUID(userUUID);

        if (!existingUser.isPresent()) {
            throw new CustomException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoDto userInfoDto = new UserInfoDto();
        userInfoDto.setUserUUID(userUUID);
        userInfoDto.setUserName(claims.get("userName", String.class));
        return userInfoDto;
    }
}

