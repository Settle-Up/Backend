package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.NetService;
import settleup.backend.domain.user.entity.UserEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Transactional
public class NetServiceImpl implements NetService {
    private final RequireTransactionRepository transactionRepo;
    private final GroupUserRepository groupUserRepo;

    @Override
    public List<NetDto> calculateNet(TransactionDto transactionDto) {
        // 주어진 group_id에 속한 모든 GroupUserEntity 조회
        List<GroupUserEntity> groupUsers =
                groupUserRepo.findByGroup_Id(transactionDto.getGroup().getId());

        // group_id에 해당하면서 상태가 CLEAR가 아닌 모든 거래를 불러옵니다.
        List<RequiresTransactionEntity> netTargetList =
                transactionRepo.findByGroupIdAndStatusNotClearAndNotInherited(transactionDto.getGroup().getId());

        Map<UserEntity, Float> userNetAmountMap = new HashMap<>();

        // groupUsers 내의 모든 유저에 대해서만 순액 계산
        for (GroupUserEntity groupUser : groupUsers) {
            for (RequiresTransactionEntity transaction : netTargetList) {
                if (transaction.getSenderUser().equals(groupUser.getUser()) || transaction.getRecipientUser().equals(groupUser.getUser())) {
                    // 송금자에 대한 처리
                    if (transaction.getSenderUser().equals(groupUser.getUser())) {
                        userNetAmountMap.merge(groupUser.getUser(), -transaction.getTransactionAmount().floatValue(), Float::sum);
                    }
                    // 수령자에 대한 처리
                    if (transaction.getRecipientUser().equals(groupUser.getUser())) {
                        userNetAmountMap.merge(groupUser.getUser(), transaction.getTransactionAmount().floatValue(), Float::sum);
                    }
                }
            }
        }

        // 계산된 순액 정보를 바탕으로 NetDto 리스트 생성
        List<NetDto> netDtoList = new ArrayList<>();
        for (Map.Entry<UserEntity, Float> entry : userNetAmountMap.entrySet()) {
            netDtoList.add(new NetDto(entry.getKey(), transactionDto.getGroup(), entry.getValue()));
        }
        return netDtoList;
    }

}
