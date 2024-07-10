package settleup.backend.domain.group.service.Impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.*;
import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.group.entity.dto.GroupMonthlyReportDto;
import settleup.backend.domain.group.repository.*;
import settleup.backend.domain.group.service.ClusterService;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.user.entity.DemoUserEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.domain.user.repository.UserRepository;

import settleup.backend.global.Helper.Status;
import settleup.backend.global.Selector.UserRepoSelector;
import settleup.backend.global.Util.UrlProvider;
import settleup.backend.global.Helper.UUID_Helper;
import settleup.backend.global.event.InviteGroupEvent;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class ClusterServiceImpl implements ClusterService {

    private GroupRepository groupRepo;
    private GroupUserRepository groupUserRepo;
    private UserRepository userRepo;
    private DemoGroupRepository demoGroupRepo;
    private DemoGroupUserRepository demoGroupUserRepo;
    private UUID_Helper uuidHelper;
    private UrlProvider urlProvider;
    private RequireTransactionRepository requireTransactionRepo;
    private UserRepoSelector selector;

    private final ApplicationEventPublisher eventPublisher;
    @PersistenceContext
    private EntityManager entityManager;

    public CreateGroupResponseDto createGroup(CreateGroupRequestDto requestDto, Boolean isRegularUser) throws CustomException {
        AbstractGroupEntity groupInfo = createAndSaveGroup(requestDto, isRegularUser);
        addUsersToGroup(requestDto.getGroupUserList(), groupInfo, isRegularUser);
        return buildCreateGroupResponseDto(groupInfo, requestDto.getGroupUserList().size(), isRegularUser);
    }

    private AbstractGroupEntity createAndSaveGroup(CreateGroupRequestDto requestDto, Boolean isRegularUser) throws CustomException {
        try {
            LocalDateTime now = LocalDateTime.now();
            AbstractGroupEntity groupInfo = isRegularUser ? new GroupEntity() : new DemoGroupEntity();
            groupInfo.setGroupName(requestDto.getGroupName());
            groupInfo.setGroupUUID(uuidHelper.UUIDForGroup());
            groupInfo.setGroupUrl(urlProvider.generateUniqueUrl());
            groupInfo.setCreatedAt(now);
            groupInfo.setGroupType(isRegularUser ? Status.REGULAR : Status.DEMO);

            if (isRegularUser) {
                groupRepo.save((GroupEntity) groupInfo);
            } else {
                demoGroupRepo.save((DemoGroupEntity) groupInfo);
            }

            return groupInfo;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GROUP_CREATION_FAILED);
        }
    }

    private void addUsersToGroup(List<String> userIds, AbstractGroupEntity groupInfo, Boolean isRegularUser) throws CustomException {
        for (String userUUID : userIds) {
            try {
                AbstractUserEntity user = selector.getUserRepository(isRegularUser).findByUserUUID(userUUID)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));


                AbstractGroupUserEntity groupUser = isRegularUser ? new GroupUserEntity() : new DemoGroupUserEntity();
                groupUser.setUser(user);
                groupUser.setGroup(groupInfo);
                groupUser.setIsMonthlyReportUpdateOn(false);

                if (isRegularUser) {
                    groupUserRepo.save((GroupUserEntity) groupUser);
                } else {
                    demoGroupUserRepo.save((DemoGroupUserEntity) groupUser);
                }
            } catch (CustomException e) {
                throw e;
            } catch (Exception e) {
                throw new CustomException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    private CreateGroupResponseDto buildCreateGroupResponseDto(AbstractGroupEntity groupInfo, int memberCount, Boolean isRegularUser) {
        List<? extends AbstractGroupUserEntity> groupUsers = selector.getGroupUserRepository(isRegularUser).findByGroup_Id(groupInfo.getId());
        List<UserInfoDto> userDtos = new ArrayList<>();
        for (AbstractGroupUserEntity groupUser : groupUsers) {
            UserInfoDto userInfoDto = new UserInfoDto(
                    groupUser.getUser().getUserUUID(),
                    groupUser.getUser().getUserName(),
                    null,
                    null,
                    null,
                    null
            );
            userDtos.add(userInfoDto);
        }

        CreateGroupResponseDto responseDto = new CreateGroupResponseDto();
        responseDto.setGroupName(groupInfo.getGroupName());
        responseDto.setGroupUrl(groupInfo.getGroupUrl());
        responseDto.setGroupId(groupInfo.getGroupUUID());
        responseDto.setGroupMemberCount(String.valueOf(memberCount));
        responseDto.setUserList(userDtos);
        return responseDto;
    }

    @Override
    public GroupMonthlyReportDto givenMonthlyReport(UserInfoDto userInfoDto, String groupId, GroupMonthlyReportDto groupMonthlyReportDto, Boolean isRegularUser) throws CustomException {
        // Retrieve the user by UUID
        AbstractUserEntity existingUser = selector.getUserRepository(isRegularUser).findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // Retrieve the group by UUID
        AbstractGroupEntity existingGroup = selector.getGroupRepository(isRegularUser).findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        // Retrieve the group-user relationship by user and group ID
        Optional<? extends AbstractGroupUserEntity> userIdAndGroupId = selector.getGroupUserRepository(isRegularUser).findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId());

        if (!userIdAndGroupId.isPresent()) {
            throw new CustomException(ErrorCode.GROUP_USER_NOT_FOUND);
        }

        AbstractGroupUserEntity groupUser = userIdAndGroupId.get();
        groupUser.setIsMonthlyReportUpdateOn(groupMonthlyReportDto.getIsMonthlyReportUpdateOn());

        // Save the updated GroupUserTypeEntity using the appropriate repository
        if (isRegularUser) {
            groupUserRepo.save((GroupUserEntity) groupUser);
        } else {
            demoGroupUserRepo.save((DemoGroupUserEntity) groupUser);
        }

        // Prepare and return the response DTO
        GroupMonthlyReportDto responseData = new GroupMonthlyReportDto();
        responseData.setUserId(existingUser.getUserUUID());
        responseData.setUserName(existingUser.getUserName());
        responseData.setGroupId(existingGroup.getGroupUUID());
        responseData.setGroupName(existingGroup.getGroupName());
        responseData.setIsMonthlyReportUpdateOn(groupMonthlyReportDto.getIsMonthlyReportUpdateOn());

        return responseData;
    }


    @Override
    public Map<String, String> deleteGroupUserInfo(UserInfoDto userInfoDto, String groupId, Boolean isRegularUser) throws CustomException {
        AbstractUserEntity existingUser = selector.getUserRepository(isRegularUser)
                .findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        AbstractGroupEntity existingGroup = selector.getGroupRepository(isRegularUser)
                .findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        List<RequiresTransactionEntity> unSettledList = requireTransactionRepo.findActiveTransactionsByGroupAndUser(existingGroup.getId(), existingUser.getId());
        if (!unSettledList.isEmpty()) {
            throw new CustomException(ErrorCode.SETTLED_REQUIRED);
        }

        GroupUserBaseRepository<? extends AbstractGroupUserEntity> groupUserRepository = selector.getGroupUserRepository(isRegularUser);
        Optional<? extends AbstractGroupUserEntity> userIdAndGroupId = groupUserRepository.findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId());

        if (userIdAndGroupId.isPresent()) {
            AbstractGroupUserEntity groupUser = userIdAndGroupId.get();
            groupUserRepository.delete(groupUser);
        } else {
            throw new CustomException(ErrorCode.GROUP_USER_NOT_FOUND);
        }

        Map<String, String> responseData = new HashMap<>();
        responseData.put("groupId", existingGroup.getGroupUUID());
        responseData.put("groupName", existingGroup.getGroupName());
        responseData.put("userId", existingUser.getUserUUID());
        responseData.put("userName", existingUser.getUserName());
        return responseData;
    }

    @Override
    public CreateGroupResponseDto inviteGroupFundamental(CreateGroupRequestDto requestDto, String groupId, Boolean isRegularUser) throws CustomException {
        UserGroupDto existingTarget = isValidIdentity(requestDto, groupId, isRegularUser);
        return inviteGroup(existingTarget, isRegularUser);
    }

    private UserGroupDto isValidIdentity(CreateGroupRequestDto requestDto, String groupUUID, Boolean isRegularUser) {
        UserGroupDto userGroupDto = new UserGroupDto();
        AbstractGroupEntity existingGroup = selector.getGroupRepository(isRegularUser)
                .findByGroupUUID(groupUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        List<AbstractUserEntity> userEntityList = new ArrayList<>();
        for (String validUser : requestDto.getGroupUserList()) {
            AbstractUserEntity existingUser = selector.getUserRepository(isRegularUser)
                    .findByUserUUID(validUser)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            userEntityList.add(existingUser);
        }

        userGroupDto.setGroup(existingGroup);
        userGroupDto.setUserEntityList(userEntityList);

        return userGroupDto;
    }

    private CreateGroupResponseDto inviteGroup(UserGroupDto existingTarget, Boolean isRegularUser) {
        List<AbstractUserEntity> userEntities = existingTarget.getUserEntityList();
        List<UserInfoDto> userInfoDtos = new ArrayList<>();

        for (AbstractUserEntity user : userEntities) {
            AbstractGroupUserEntity groupUser;
            if (isRegularUser) {
                GroupUserEntity regularGroupUser = new GroupUserEntity();
                regularGroupUser.setGroup((GroupEntity) existingTarget.getGroup());
                regularGroupUser.setUser((UserEntity) user);
                regularGroupUser.setIsMonthlyReportUpdateOn(false);
                groupUserRepo.save(regularGroupUser);
                groupUser = regularGroupUser; // Only for carrying forward common processing
            } else {
                DemoGroupUserEntity demoGroupUser = new DemoGroupUserEntity();
                demoGroupUser.setGroup((DemoGroupEntity) existingTarget.getGroup());
                demoGroupUser.setUser((DemoUserEntity) user);
                demoGroupUser.setIsMonthlyReportUpdateOn(false);
                demoGroupUserRepo.save(demoGroupUser);
                groupUser = demoGroupUser; // Only for carrying forward common processing
            }

            UserInfoDto userInfoDto = new UserInfoDto();
            userInfoDto.setUserEmail(user.getUserEmail());
            userInfoDto.setUserName(user.getUserName());
            userInfoDto.setUserId(user.getUserUUID());
            userInfoDtos.add(userInfoDto);
        }
        entityManager.flush(); // Ensure all pending saves are completed

        CreateGroupResponseDto responseDto = new CreateGroupResponseDto();
        responseDto.setGroupId(existingTarget.getGroup().getGroupUUID());
        responseDto.setGroupName(existingTarget.getGroup().getGroupName());
        responseDto.setGroupUrl(existingTarget.getGroup().getGroupUrl());
        responseDto.setGroupMemberCount(String.valueOf(userInfoDtos.size()));
        responseDto.setUserList(userInfoDtos);
        eventPublisher.publishEvent(new InviteGroupEvent(this, responseDto));

        return responseDto;
    }
}
