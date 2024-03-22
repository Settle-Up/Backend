package settleup.backend.domain.group.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        List<GroupInfoListDto.userGroupListDto> groupList = new ArrayList<>();

        for (GroupUserEntity groupUser : userGroupList) {
            TransactionDto transactionDto = new TransactionDto();
            transactionDto.setGroup(groupUser.getGroup());
            List<NetDto> groupAllNetList = netService.calculateNet(transactionDto);

            // Receipt 조회
            List<ReceiptEntity> receipts = receiptRepo.findReceiptByGroupId(groupUser.getGroup().getId());
            // receipts가 비어있지 않다면 첫 번째 요소의 createdAt을 사용, 그렇지 않다면 "N/A"
            String lastActive = receipts.isEmpty() ? "N/A" : receipts.get(0).getCreatedAt().toString();

            for (NetDto netDto : groupAllNetList) {
                if (netDto.getUser().equals(existingUser)) {
                    GroupInfoListDto.userGroupListDto groupInfo = new GroupInfoListDto.userGroupListDto(
                            groupUser.getGroup().getId().toString(),
                            groupUser.getGroup().getGroupName(), // 가정한 메서드명
                            netDto.getNetAmount(),
                            lastActive
                    );

                    groupList.add(groupInfo);
                    break; // 현재 사용자에 대한 정보를 찾았으므로 루프 중지
                }
            }
        }

        return new GroupInfoListDto(existingUser.getUserUUID(), existingUser.getUserName(), groupList);
    }
}
