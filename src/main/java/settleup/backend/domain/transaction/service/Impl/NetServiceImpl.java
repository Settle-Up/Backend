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
import settleup.backend.domain.user.entity.dto.UserGroupDto;

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
    public List<NetDto> calculateNet(UserGroupDto group) {

        List<GroupUserEntity> groupUsers =
                groupUserRepo.findByGroup_Id(group.getGroup().getId());


        List<RequiresTransactionEntity> netTargetList =
                transactionRepo.findByGroupIdAndRequiredReflection(group.getGroup().getId());

        Map<UserEntity, Float> userNetAmountMap = new HashMap<>();


        for (GroupUserEntity groupUser : groupUsers) {
            for (RequiresTransactionEntity transaction : netTargetList) {
                if (transaction.getSenderUser().equals(groupUser.getUser()) || transaction.getRecipientUser().equals(groupUser.getUser())) {

                    if (transaction.getSenderUser().equals(groupUser.getUser())) {
                        userNetAmountMap.merge(groupUser.getUser(), -transaction.getTransactionAmount().floatValue(), Float::sum);
                    }

                    if (transaction.getRecipientUser().equals(groupUser.getUser())) {
                        userNetAmountMap.merge(groupUser.getUser(), transaction.getTransactionAmount().floatValue(), Float::sum);
                    }
                }
            }
        }


        List<NetDto> netDtoList = new ArrayList<>();
        for (Map.Entry<UserEntity, Float> entry : userNetAmountMap.entrySet()) {
            netDtoList.add(new NetDto(entry.getKey(), group.getGroup(), entry.getValue()));
        }
        return netDtoList;
    }

}
