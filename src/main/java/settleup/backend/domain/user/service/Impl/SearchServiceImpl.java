package settleup.backend.domain.user.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.SearchService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final UserRepository userRepo;

    @Override
    public Page<UserInfoDto> getUserList(String partOfEmail, Pageable pageable, UserInfoDto userInfoDto) {
        Optional<UserEntity> loginUser =userRepo.findByUserUUID(userInfoDto.getUserId());
        String notContainUserEmail =loginUser.get().getUserEmail();
        Page<UserEntity> userEntities = userRepo.findByUserEmailContainingAndUserEmailNot(partOfEmail, notContainUserEmail, pageable);
        return userEntities.map(this::toUserInfo);
    }

    private UserInfoDto toUserInfo(UserEntity userEntity) {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setUserId(userEntity.getUserUUID());
        userInfo.setUserEmail(userEntity.getUserEmail());
        return userInfo;
    }
}