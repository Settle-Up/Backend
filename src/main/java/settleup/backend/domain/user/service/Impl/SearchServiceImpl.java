package settleup.backend.domain.user.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.group.entity.AbstractGroupUserEntity;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.SearchService;
import settleup.backend.global.Selector.UserRepoSelector;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final UserRepoSelector userRepoSelector;

    @Override
    public Page<UserInfoDto> getUserList(String partOfEmail, Pageable pageable, UserInfoDto userInfoDto) {
        AbstractUserEntity loginUser = userRepoSelector.getUserRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        String notContainUserEmail = loginUser.getUserEmail();
        Page<? extends AbstractUserEntity> userPage = userRepoSelector.getUserRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByUserEmailContainingAndUserEmailNot(partOfEmail, notContainUserEmail, pageable);

        List<UserInfoDto> userInfoDtos = new ArrayList<>();
        for (AbstractUserEntity user : userPage.getContent()) {
            userInfoDtos.add(toUserInfo(user));
        }

        return new PageImpl<>(userInfoDtos, pageable, userPage.getTotalElements());
    }

    @Override
    public Page<UserInfoDto> getUserListNotIncludeGroupUser(String partOfEmail, Pageable pageable, UserInfoDto userInfoDto, String groupId) {
        AbstractGroupEntity group = userRepoSelector.getGroupRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        List<Long> excludedUserIds = new ArrayList<>();
        List<? extends AbstractGroupUserEntity> groupUsers = userRepoSelector.getGroupUserRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByGroup_Id(group.getId());

        for (AbstractGroupUserEntity groupUser : groupUsers) {
            excludedUserIds.add(groupUser.getUser().getId());
        }

        Page<? extends AbstractUserEntity> userPage = userRepoSelector.getUserRepository(userInfoDto.getIsRegularUserOrDemoUser())
                .findByEmailExcludingUsers(partOfEmail, excludedUserIds, pageable);

        List<UserInfoDto> userInfoDtos = new ArrayList<>();
        for (AbstractUserEntity user : userPage.getContent()) {
            userInfoDtos.add(toUserInfo(user));
        }

        return new PageImpl<>(userInfoDtos, pageable, userPage.getTotalElements());
    }

    private UserInfoDto toUserInfo(AbstractUserEntity userEntity) {
        UserInfoDto userInfo = new UserInfoDto();
        userInfo.setUserId(userEntity.getUserUUID());
        userInfo.setUserEmail(userEntity.getUserEmail());
        return userInfo;
    }
}






