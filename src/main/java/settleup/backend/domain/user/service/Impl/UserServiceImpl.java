package settleup.backend.domain.user.service.Impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.group.entity.AbstractGroupUserEntity;
import settleup.backend.domain.group.entity.DemoGroupUserEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.dto.GroupMonthlyReportDto;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.domain.user.entity.DemoUserEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.DemoUserRepository;
import settleup.backend.domain.user.repository.UserBaseRepository;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.UserService;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Selector.UserRepoSelector;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepoSelector selector;
    private final UserRepository userRepo;
    private final DemoUserRepository demoUserRepo;

    @PersistenceContext
    private EntityManager entityManager;


    @Override
    public Map<String, Object> clusterUserDecimal(UserInfoDto userInfoDto) throws CustomException {
        Boolean isUserType=userInfoDto.getIsRegularUserOrDemoUser();

        AbstractUserEntity existingUser = selector.getUserRepository(isUserType).findByUserUUID(userInfoDto.getUserId())
           .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        existingUser.setIsDecimalInputOption(userInfoDto.getIsDecimalInputOption());

        if(isUserType){
            userRepo.save((UserEntity) existingUser);

        }else {
            demoUserRepo.save((DemoUserEntity) existingUser);
        }
        entityManager.flush();
        return buildResponseData(existingUser);
    }



    private Map<String, Object> buildResponseData(AbstractUserEntity user) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("userId", user.getUserUUID());
        responseData.put("userName", user.getUserName());
        responseData.put("isDecimalInputOption", user.getIsDecimalInputOption());
        return responseData;
    }

    @Override
    public Map<String, Object> retrievedUserDecimal(UserInfoDto userInfoDto) throws CustomException {
        AbstractUserEntity existingUser = selector.getUserRepository(userInfoDto.getIsRegularUserOrDemoUser()).findByUserUUID(userInfoDto.getUserId()).
                orElseThrow(()->new CustomException(ErrorCode.USER_NOT_FOUND));
        Map<String,Object> response= new HashMap<>();
        response.put("userId", userInfoDto.getUserId());
        response.put("userName", userInfoDto.getUserName());
        response.put("isDecimalInputOption", existingUser.getIsDecimalInputOption());
        return response;
    }
}