package settleup.backend.domain.group.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.group.service.ClusterService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.Util.UrlProvider;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@AllArgsConstructor
public class ClusterServiceImpl implements ClusterService {

    private GroupRepository groupRepository;
    private GroupUserRepository groupUserRepository;
    private UserRepository userRepository;
    private UUID_Helper uuidHelper;
    private UrlProvider urlProvider;

    /**
     * createGroup 그룹생성
     *
     * @param requestDto CreateGroupRequestDto groupName ,groupMemeberCount, groupUserList
     * @requiredProcess 1. create group(createuuid) 2. save 3. add user in group
     * @privateMethod createAndSaveGroup(requestDto) /process:/ create uuid then save groupEntity
     * @privateMethod addUsersToGroup(List userIds,groupInfo) /process:/ list of userId and adds each specific group (create groupUserInfo)
     * @privateMethod buildCreateGroupResponseDto
     * @return CreateGroupResponseDto -> group's uuid,name,memberCount, url, userList
     * @throws CustomException a.-> uuid generation, url generation, or db saving
     *                         b.-> client 에서 받은 userList 에서 유저 리스트 못찾음 ,or db saving
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
            groupRepository.save(groupInfo);
            return groupInfo;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.GROUP_CREATION_FAILED);
        }
    }

    private void addUsersToGroup(List<String> userIds, GroupEntity groupInfo) throws CustomException {
        for (String userUUID : userIds) {
            try {
                UserEntity user = userRepository.findByUserUUID(userUUID)
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                GroupUserEntity groupUser = new GroupUserEntity();
                groupUser.setUser(user);
                groupUser.setGroup(groupInfo);
                groupUser.setMonthlyReportUpdateOn(false);
                groupUserRepository.save(groupUser);
            } catch (CustomException e) {
                throw new CustomException(ErrorCode.DATABASE_ERROR);
            } catch (Exception e) {
                throw e;
            }
        }
    }

    private CreateGroupResponseDto buildCreateGroupResponseDto(GroupEntity groupInfo, int memberCount) {
        List<GroupUserEntity> groupUsers = groupUserRepository.findByGroup_Id(groupInfo.getId());
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
}