package settleup.backend.domain.group.service.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.group.entity.dto.GroupMonthlyReportDto;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.group.service.ClusterService;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.domain.user.service.EmailSenderService;
import settleup.backend.domain.user.service.KakaoService;
import settleup.backend.global.Util.UrlProvider;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ClusterServiceImpl implements ClusterService {

    private GroupRepository groupRepo;
    private GroupUserRepository groupUserRepo;
    private UserRepository userRepo;
    private UUID_Helper uuidHelper;
    private UrlProvider urlProvider;
    private RequireTransactionRepository requireTransactionRepo;
    private EmailSenderService emailSenderService;



    /**
     * createGroup 그룹생성
     *
     * @param requestDto CreateGroupRequestDto groupName ,groupMemeberCount, groupUserList
     * @return CreateGroupResponseDto -> group's uuid,name,memberCount, url, userList
     * @throws CustomException a.-> uuid generation, url generation, or db saving
     *                         b.-> client 에서 받은 userList 에서 유저 리스트 못찾음 ,or db saving
     * @requiredProcess 1. create group(createuuid) 2. save 3. add user in group
     * @privateMethod createAndSaveGroup(requestDto) /process:/ create uuid then save groupEntity
     * @privateMethod addUsersToGroup(List userIds, groupInfo) /process:/ list of userId and adds each specific group (create groupUserInfo)
     * @privateMethod buildCreateGroupResponseDto
     * @method A. createAndSaveGroup ,B. addUserToGroup ,C. buildCreateGroupResponseDto
     */
    @Override
    public CreateGroupResponseDto createGroup(CreateGroupRequestDto requestDto) throws CustomException {
        GroupEntity groupInfo = createAndSaveGroup(requestDto);
        addUsersToGroup(requestDto.getGroupUserList(), groupInfo);
        return buildCreateGroupResponseDto(groupInfo, requestDto.getGroupUserList().size());
    }


    private GroupEntity createAndSaveGroup(CreateGroupRequestDto requestDto) throws CustomException {
        try {
            LocalDateTime now = LocalDateTime.now();
            GroupEntity groupInfo = new GroupEntity();
            groupInfo.setGroupName(requestDto.getGroupName());
            groupInfo.setGroupUUID(uuidHelper.UUIDForGroup());
            groupInfo.setGroupUrl(urlProvider.generateUniqueUrl());
            groupInfo.setCreationTime(now);
            groupRepo.save(groupInfo);
            return groupInfo;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GROUP_CREATION_FAILED);
        }
    }

    private void addUsersToGroup(List<String> userIds, GroupEntity groupInfo) throws CustomException {
        for (String userUUID : userIds) {
            try {
                UserEntity user = userRepo.findByUserUUID(userUUID)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                GroupUserEntity groupUser = new GroupUserEntity();
                groupUser.setUser(user);
                groupUser.setGroup(groupInfo);
                groupUser.setIsMonthlyReportUpdateOn(false);
                groupUserRepo.save(groupUser);
            } catch (CustomException e) {
                throw e;
            } catch (Exception e) {
                throw new CustomException(ErrorCode.DATABASE_ERROR);
            }
        }
    }

    private CreateGroupResponseDto buildCreateGroupResponseDto(GroupEntity groupInfo, int memberCount) {
        List<GroupUserEntity> groupUsers = groupUserRepo.findByGroup_Id(groupInfo.getId());
        List<UserInfoDto> userDtos = groupUsers.stream().map(groupUser -> new UserInfoDto(
                groupUser.getUser().getUserUUID(),
                groupUser.getUser().getUserName(),
                null,
                null
        )).collect(Collectors.toList());

        CreateGroupResponseDto responseDto = new CreateGroupResponseDto();
        responseDto.setGroupName(groupInfo.getGroupName());
        responseDto.setGroupUrl(groupInfo.getGroupUrl());
        responseDto.setGroupId(groupInfo.getGroupUUID());
        responseDto.setGroupMemberCount(String.valueOf(memberCount));
        responseDto.setUserList(userDtos);
        return responseDto;
    }


    @Override
    public List<UserInfoDto> getGroupUserInfo(String groupUUID) throws CustomException {
        try {
            Optional<GroupEntity> existingGroup = Optional.ofNullable(groupRepo.findByGroupUUID(groupUUID)
                    .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND)));
            Long groupPrimaryId = existingGroup.get().getId();
            List<GroupUserEntity> userList = groupUserRepo.findByGroup_Id(groupPrimaryId);
            List<UserInfoDto> userInfoDtoList = new ArrayList<>();
            for (GroupUserEntity userEntity : userList) {
                UserInfoDto userInfoDto = new UserInfoDto();
                userInfoDto.setUserId(userEntity.getUser().getUserUUID());
                userInfoDto.setUserName(userEntity.getUser().getUserName());
                userInfoDtoList.add(userInfoDto);
            }
            return userInfoDtoList;
        } catch (CustomException e) {
            throw e;
        }
    }


    @Override
    public GroupMonthlyReportDto givenMonthlyReport(UserInfoDto userInfoDto, String groupId, GroupMonthlyReportDto groupMonthlyReportDto) throws CustomException {

        UserEntity existingUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        GroupEntity existingGroup = groupRepo.findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));


        Optional<GroupUserEntity> userIdAndGroupId = groupUserRepo.findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId());
        if (userIdAndGroupId.isPresent()) {
            GroupUserEntity groupUser = userIdAndGroupId.get();

            groupUser.setIsMonthlyReportUpdateOn(groupMonthlyReportDto.getIsMonthlyReportUpdateOn());
            groupUserRepo.save(groupUser);

        } else {

            throw new CustomException(ErrorCode.GROUP_USER_NOT_FOUND);
        }

        GroupMonthlyReportDto responseData = new GroupMonthlyReportDto();
        responseData.setUserId(existingUser.getUserUUID());
        responseData.setUserName(existingUser.getUserName());
        responseData.setGroupId(existingGroup.getGroupUUID());
        responseData.setGroupName(existingGroup.getGroupName());
        responseData.setIsMonthlyReportUpdateOn(groupMonthlyReportDto.getIsMonthlyReportUpdateOn());


        return responseData;
    }

    @Override
    public Map<String, String> deleteGroupUserInfo(UserInfoDto userInfoDto, String groupId) throws CustomException {
        UserEntity existingUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        GroupEntity existingGroup = groupRepo.findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        List<RequiresTransactionEntity> unSettledList = requireTransactionRepo.findByGroupAndUserAndStatusNotClearAndNotInherited(existingGroup.getId(), existingUser.getId());
        if (!unSettledList.isEmpty()) {
            throw new CustomException(ErrorCode.SETTLED_REQUIRED);
        }

        Optional<GroupUserEntity> userIdAndGroupId = groupUserRepo.findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId());
        if (userIdAndGroupId.isPresent()) {
            GroupUserEntity groupUser = userIdAndGroupId.get();

            groupUserRepo.delete(groupUser);

            Map<String, String> responseData = new HashMap<>();
            responseData.put("groupId", existingGroup.getGroupUUID());
            responseData.put("groupName", existingGroup.getGroupName());
            responseData.put("userId", existingUser.getUserUUID());
            responseData.put("userName", existingUser.getUserName());
            return responseData;
        } else {
            throw new CustomException(ErrorCode.GROUP_USER_NOT_FOUND);
        }
    }

    @Override
    public CreateGroupResponseDto inviteGroupFundamental(CreateGroupRequestDto requestDto,String groupId) throws CustomException {
       UserGroupDto existingTarget = isValidIdentity(requestDto,groupId);
       Map<String,String> noticeMap = inviteGroup(existingTarget);
       emailSenderService.sendEmailToNewGroupUser(noticeMap);

       return null;
    }


    private UserGroupDto isValidIdentity(CreateGroupRequestDto requestDto, String groupUUID) {
        UserGroupDto userGroupDto =new UserGroupDto();
        GroupEntity existingGroup = groupRepo.findByGroupUUID(groupUUID).
                orElseThrow(()-> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        List<UserEntity> userEntityList = new ArrayList<>();

        for (String validUser : requestDto.getGroupUserList()) {
            UserEntity existingUser = userRepo.findByUserUUID(validUser)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
            userEntityList.add(existingUser);
        }

        userGroupDto.setGroup(existingGroup);
        userGroupDto.setUserEntityList(userEntityList);

        return userGroupDto;
    }

    private Map<String,String> inviteGroup(UserGroupDto existingTarget) {
        List<UserEntity> userEntities = existingTarget.getUserEntityList();
        Map<String,String> mapForInviteNotice= new HashMap<>();
        for(UserEntity user :userEntities){
            GroupUserEntity groupUser = new GroupUserEntity();
            groupUser.setGroup(existingTarget.getGroup());
            groupUser.setUser(user);
            groupUser.setIsMonthlyReportUpdateOn(false);
            groupUserRepo.save(groupUser);
            mapForInviteNotice.put(user.getUserName(), user.getUserEmail());
        }
        return mapForInviteNotice;
    }

}
