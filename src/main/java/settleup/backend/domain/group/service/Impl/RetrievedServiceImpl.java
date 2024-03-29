package settleup.backend.domain.group.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.dto.GroupInfoListDto;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.group.service.RetrievedService;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.service.NetService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class RetrievedServiceImpl implements RetrievedService {
    private final UserRepository userRepo;
    private final GroupUserRepository groupUserRepo;
    private final NetService netService;
    private final ReceiptRepository receiptRepo;

    @Override
    public GroupInfoListDto getGroupInfoByUser(UserInfoDto userInfoDto) throws CustomException {
        UserEntity existingUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<GroupUserEntity> userGroupList = groupUserRepo.findByUser_Id(existingUser.getId());
        List<GroupInfoListDto.UserGroupListDto> groupList = new ArrayList<>();

        for (GroupUserEntity groupUser : userGroupList) {
            GroupEntity group = groupUser.getGroup();
            GroupInfoListDto.UserGroupListDto groupInfoDto = new GroupInfoListDto.UserGroupListDto();
            groupInfoDto.setGroupId(group.getGroupUUID());
            groupInfoDto.setGroupName(group.getGroupName());
            int groupMemberCount = groupUserRepo.findByGroup_Id(group.getId()).size();
            groupInfoDto.setGroupMemberCount(String.valueOf(groupMemberCount));

            // Receipt 조회
            List<ReceiptEntity> receipts = receiptRepo.findReceiptByGroupId(groupUser.getGroup().getId());
            String lastActive = receipts.isEmpty() ? null : receipts.get(0).getCreatedAt().toString();
            groupInfoDto.setLastActive(lastActive);

            // Net 계산
            TransactionDto transactionDto = new TransactionDto();
            transactionDto.setGroup(group);
            List<NetDto> groupAllNetList = netService.calculateNet(transactionDto);

            for (NetDto netDto : groupAllNetList) {
                if (netDto.getUser().equals(existingUser)) {
                    Float netAmount = netDto.getNetAmount();
                    if (netAmount != null) {
                        groupInfoDto.setSettlementBalance(String.valueOf(netAmount));
                        groupInfoDto.setLastActive(lastActive);
                    }
                    break;
                }
            }

            groupList.add(groupInfoDto);
        }

        return new GroupInfoListDto(existingUser.getUserUUID(), existingUser.getUserName(), groupList);
    }
}