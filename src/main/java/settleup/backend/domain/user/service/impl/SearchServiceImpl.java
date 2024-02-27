package settleup.backend.domain.user.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.SearchService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final UserRepository userRepository;

    @Override
    public List<UserInfoDto> getUserList(String partOfEmail) {
        List<UserEntity> userEntities = userRepository.findByUserEmailContaining(partOfEmail);
        return userEntities.stream().map(this::toUserInfo).collect(Collectors.toList());
    }

    private UserInfoDto toUserInfo(UserEntity userEntity) {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setUserEmail(userEntity.getUserEmail());
        userInfo.setUserUUID(userEntity.getUserUUID());
        return userInfo;
    }
}