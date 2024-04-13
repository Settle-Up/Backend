package settleup.backend.domain.user.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.SearchService;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final UserRepository userRepo;
    private final GroupUserRepository groupUserRepo;
    private final GroupRepository groupRepo;

    @Override
    public Page<UserInfoDto> getUserList(String partOfEmail, Pageable pageable, UserInfoDto userInfoDto) {
        UserEntity loginUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String notContainUserEmail = loginUser.getUserEmail();
        return userRepo.findByUserEmailContainingAndUserEmailNot(partOfEmail, notContainUserEmail, pageable)
                .map(this::toUserInfo);
    }

    @Override
    public Page<UserInfoDto> getUserListNotIncludeGroupUser(String partOfEmail, Pageable pageable, UserInfoDto userInfoDto, String groupId) {
        GroupEntity group = groupRepo.findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        List<Long> excludedUserIds = groupUserRepo.findByGroup_Id(group.getId())
                .stream()
                .map(groupUserEntity -> groupUserEntity.getUser().getId())
                .collect(Collectors.toList());
        return userRepo.findByEmailExcludingUsers(partOfEmail, excludedUserIds, pageable)
                .map(this::toUserInfo);
    }

    private UserInfoDto toUserInfo(UserEntity userEntity) {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setUserId(userEntity.getUserUUID());
        userInfo.setUserEmail(userEntity.getUserEmail());
        return userInfo;
    }
}


