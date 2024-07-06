package settleup.backend.domain.user.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.DemoGroupEntity;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
import settleup.backend.domain.group.entity.GroupUserTypeEntity;
import settleup.backend.domain.group.repository.DemoGroupRepository;
import settleup.backend.domain.group.repository.DemoGroupUserRepository;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.user.entity.DemoUserEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.entity.UserTypeEntity;
import settleup.backend.domain.user.repository.DemoUserRepository;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.SearchService;
import settleup.backend.global.Selector.UserRepoSelector;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final UserRepoSelector userRepoSelector;

    @Override
    public Page<UserInfoDto> getUserList(String partOfEmail, Pageable pageable, UserInfoDto userInfoDto) {
        UserTypeEntity loginUser = userRepoSelector.getUserRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String notContainUserEmail = loginUser.getUserEmail();
        Page<? extends UserTypeEntity> userPage = userRepoSelector.getUserRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByUserEmailContainingAndUserEmailNot(partOfEmail, notContainUserEmail, pageable);

        List<UserInfoDto> userInfoDtos = new ArrayList<>();
        for (UserTypeEntity user : userPage.getContent()) {
            userInfoDtos.add(toUserInfo(user));
        }

        return new PageImpl<>(userInfoDtos, pageable, userPage.getTotalElements());
    }

    @Override
    public Page<UserInfoDto> getUserListNotIncludeGroupUser(String partOfEmail, Pageable pageable, UserInfoDto userInfoDto, String groupId) {
        GroupTypeEntity group = userRepoSelector.getGroupRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        List<Long> excludedUserIds = new ArrayList<>();
        List<? extends GroupUserTypeEntity> groupUsers = userRepoSelector.getGroupUserRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByGroup_Id(group.getId());

        for (GroupUserTypeEntity groupUser : groupUsers) {
            excludedUserIds.add(groupUser.getUser().getId());
        }

        Page<? extends UserTypeEntity> userPage = userRepoSelector.getUserRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByEmailExcludingUsers(partOfEmail, excludedUserIds, pageable);

        List<UserInfoDto> userInfoDtos = new ArrayList<>();
        for (UserTypeEntity user : userPage.getContent()) {
            userInfoDtos.add(toUserInfo(user));
        }

        return new PageImpl<>(userInfoDtos, pageable, userPage.getTotalElements());
    }

    private UserInfoDto toUserInfo(UserTypeEntity userEntity) {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setUserId(userEntity.getUserUUID());
        userInfo.setUserEmail(userEntity.getUserEmail());
        return userInfo;
    }
}






