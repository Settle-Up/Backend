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
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final UserRepository userRepository;

    @Override
    public Page<UserInfoDto> getUserList(String partOfEmail, Pageable pageable) {
        Page<UserEntity> userEntities = userRepository.findByUserEmailContaining(partOfEmail, pageable);
        return userEntities.map(this::toUserInfo);
    }

    // UserEntity를 UserInfoDto로 변환하는 메서드
    private UserInfoDto toUserInfo(UserEntity userEntity) {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setUserId(userEntity.getUserUUID());
        userInfo.setUserEmail(userEntity.getUserEmail());
        return userInfo;
    }
}