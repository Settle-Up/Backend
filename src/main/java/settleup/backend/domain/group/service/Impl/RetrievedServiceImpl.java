package settleup.backend.domain.group.service.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.group.entity.AbstractGroupUserEntity;
import settleup.backend.domain.group.entity.dto.GroupInfoListDto;
import settleup.backend.domain.group.repository.CustomGroupUserRepository;
import settleup.backend.domain.group.repository.GroupUserBaseRepository;
import settleup.backend.domain.group.service.RetrievedService;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.service.NetService;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Selector.UserRepoSelector;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Transactional
public class RetrievedServiceImpl implements RetrievedService {

    private final CustomGroupUserRepository customGroupUserRepo;
    private final NetService netService;
    private final ReceiptRepository receiptRepo;
    private final UserRepoSelector selector;
    private static final Logger logger = LoggerFactory.getLogger(RetrievedServiceImpl.class);

    @Override
    public GroupInfoListDto getGroupInfoByUser(UserInfoDto userInfoDto, Pageable pageable) throws CustomException {
        Boolean isUserType = userInfoDto.getIsRegularUserOrDemoUser();
        if (isUserType == null) {
            logger.error("isRegularUserOrDemoUser is null for userUUID: {}", userInfoDto.getUserId());
            throw new CustomException(ErrorCode.USER_TYPE_CANNOT_BE_NULL);
        }
        logger.debug("Starting getGroupInfoByUser: isRegularUserOrDemoUser = {}", isUserType);

        // Retrieve the existing user
        AbstractUserEntity existingUser = selector.getUserRepository(isUserType).findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        logger.debug("Existing user found: UserType = {}, UserID = {}", existingUser.getUserType(), existingUser.getId());

        System.out.println("existingUser: " + existingUser.getUserType() + " " + existingUser.getId());
        // Use ternary operator to select the appropriate group user repository
        GroupUserBaseRepository<? extends AbstractGroupUserEntity> groupUserRepository = selector.getGroupUserRepository(isUserType);

        // Log which repository is being selected
        logger.debug("Selected repository: {}", isUserType ? "Regular User Repository" : "Demo User Repository");

        System.out.println("여기봐봐 :" + isUserType);
        Page<AbstractGroupUserEntity> userGroupPage = customGroupUserRepo.findByUserIdWithLatestReceiptOrCreatedAt(existingUser.getId(), pageable,isUserType);

        List<GroupInfoListDto.UserGroupListDto> groupList = new ArrayList<>();
        for (AbstractGroupUserEntity groupUser : userGroupPage.getContent()) {
            AbstractGroupEntity group = groupUser.getGroup();
            if (group == null) {
                logger.error("Group entity is null for userID = {}", existingUser.getId());
                continue;
            }

            logger.debug("Processing group: GroupID = {}, GroupName = {}", group.getGroupUUID(), group.getGroupName());
            GroupInfoListDto.UserGroupListDto groupInfoDto = new GroupInfoListDto.UserGroupListDto();
            groupInfoDto.setGroupId(group.getGroupUUID());
            groupInfoDto.setGroupName(group.getGroupName());

            // Calculate the number of group members
            int groupMemberCount = groupUserRepository.findByGroup_Id(group.getId()).size();
            logger.debug("Group member count for GroupID = {}: {}", group.getGroupUUID(), groupMemberCount);
            groupInfoDto.setGroupMemberCount(String.valueOf(groupMemberCount));

            // Fetch receipts
            List<ReceiptEntity> receipts = receiptRepo.findReceiptByGroupId(groupUser.getGroup().getId());
            String lastActive = receipts.isEmpty() ? null : receipts.get(0).getCreatedAt().toString();
            groupInfoDto.setLastActive(lastActive);

            // Check receipt registration
            boolean isReceiptRegistered = netService.isReceiptRegisteredInGroup(group.getId());
            logger.debug("Receipt registered for group {}: {}", group.getGroupUUID(), isReceiptRegistered);

            // Calculate final settlement balance
            String formattedNetAmount = calculateNetAmount(existingUser, group, isReceiptRegistered);
            logger.debug("Final settlement balance for group {}: {}", group.getGroupUUID(), formattedNetAmount);
            groupInfoDto.setSettlementBalance(formattedNetAmount);
            groupList.add(groupInfoDto);
        }

        boolean hasNextPage = userGroupPage.hasNext();
        logger.debug("Has next page: {}", hasNextPage);
        return new GroupInfoListDto(existingUser.getUserUUID(), existingUser.getUserName(), hasNextPage, groupList);
    }

    private String calculateNetAmount(AbstractUserEntity existingUser, AbstractGroupEntity group, boolean isReceiptRegistered) throws CustomException {
        if (!isReceiptRegistered) {
            return null;
        }

        Boolean userType = null;
        if (existingUser.getUserType() == Status.DEMO) {
            userType = false;
        } else if (existingUser.getUserType() == Status.REGULAR) {
            userType = true;
        }
        UserGroupDto groupDto = new UserGroupDto();
        groupDto.setGroup(group);
        groupDto.setIsUserType(userType);
        List<NetDto> groupAllNetList = netService.calculateNet(groupDto);

        logger.debug("Net calculation result for group {}: {}", group.getGroupUUID(), groupAllNetList);

        for (NetDto netDto : groupAllNetList) {
            if (netDto.getUser().equals(existingUser)) {
                BigDecimal netAmount = netDto.getNetAmount();
                logger.debug("Net amount for user {}: {}", existingUser.getId(), netAmount);
                return String.format("%.2f", netAmount);
            }
        }
        return "0.00";
    }

    @Override
    public List<UserInfoDto> getGroupUserInfo(String groupUUID, Boolean isRegularUser) throws CustomException {
        AbstractGroupEntity existingGroup = selector.getGroupRepository(isRegularUser).findByGroupUUID(groupUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        Long groupPrimaryId = existingGroup.getId();
        List<AbstractGroupUserEntity> userList = (List<AbstractGroupUserEntity>) selector.getGroupUserRepository(isRegularUser).findByGroup_Id(groupPrimaryId);
        List<UserInfoDto> userInfoDtoList = new ArrayList<>();
        for (AbstractGroupUserEntity userEntity : userList) {
            UserInfoDto userInfoDto = new UserInfoDto();
            userInfoDto.setUserId(userEntity.getUser().getUserUUID());
            userInfoDto.setUserEmail(userEntity.getUser().getUserEmail());
            userInfoDto.setUserName(userEntity.getUser().getUserName());
            userInfoDtoList.add(userInfoDto);
        }
        return userInfoDtoList;
    }
}
