package settleup.backend.domain.group.service.Impl;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.dto.GroupOverviewDto;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.service.OverviewService;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.service.NetService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class OverviewServiceImpl implements OverviewService {
    private final GroupRepository groupRepo;
    private final NetService netService;

    @Override
    public GroupOverviewDto retrievedOverview(String groupUUID, UserInfoDto userInfoDto) throws CustomException {
        GroupOverviewDto overviewDto = new GroupOverviewDto();
        overviewDto.setUserId(userInfoDto.getUserId());
        overviewDto.setUserName(userInfoDto.getUserName());
        Optional<GroupEntity> existingGroup = Optional.ofNullable(groupRepo.findByGroupUUID(groupUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND)));
        overviewDto.setGroupId(existingGroup.get().getGroupUUID());
        overviewDto.setGroupName(existingGroup.get().getGroupName());
        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setGroup(existingGroup.get());
        overviewDto.setSettlementBalance(String.valueOf(netService.calculateNet(transactionDto)));


        return null;
    }
}
