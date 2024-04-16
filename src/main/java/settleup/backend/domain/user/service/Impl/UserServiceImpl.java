package settleup.backend.domain.user.service.Impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.UserService;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, String> clusterUserDecimal(UserInfoDto userInfoDto) throws CustomException {
        UserEntity user = isValidUser(userInfoDto);
        user.setIsDecimalInputOption(userInfoDto.getIsDecimalInputOption());
        userRepo.save(user);
        entityManager.flush();
        return buildResponseData(user);
    }

    private UserEntity isValidUser(UserInfoDto userInfoDto) {
        UserEntity existingUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return existingUser;
    }

    private Map<String, String> buildResponseData(UserEntity user) {
        Map<String, String> responseData = new HashMap<>();
        responseData.put("userId", user.getUserUUID());
        responseData.put("userName", user.getUserName());
        responseData.put("isDecimalInputOption", String.valueOf(user.getIsDecimalInputOption()));
        return responseData;
    }
}
