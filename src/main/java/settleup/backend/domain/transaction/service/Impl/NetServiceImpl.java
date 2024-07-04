package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.group.service.Impl.RetrievedServiceImpl;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.NetService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserGroupDto;

import java.math.BigDecimal;
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
    private final ReceiptRepository receiptRepo;

    private static final Logger logger = LoggerFactory.getLogger(NetServiceImpl.class);

    @Override
    public List<NetDto> calculateNet(UserGroupDto group) {
        List<GroupUserEntity> groupUsers = groupUserRepo.findByGroup_Id(group.getGroup().getId());
        List<RequiresTransactionEntity> netTargetList = transactionRepo.findByGroupIdAndRequiredReflection(group.getGroup().getId());
        Map<UserEntity, BigDecimal> userNetAmountMap = new HashMap<>();

        logger.debug("Group Users: {}", groupUsers);
        logger.debug("Net Target List: {}", netTargetList);

        // 영수증이 등록된 경우에 대한 처리
        if (netTargetList.isEmpty() && isReceiptRegisteredInGroup(group.getGroup().getId())) {
            for (GroupUserEntity groupUser : groupUsers) {
                userNetAmountMap.put(groupUser.getUser(), BigDecimal.ZERO);
            }
        } else {
            for (GroupUserEntity groupUser : groupUsers) {
                for (RequiresTransactionEntity transaction : netTargetList) {
                    if (transaction.getSenderUser().equals(groupUser.getUser()) || transaction.getRecipientUser().equals(groupUser.getUser())) {
                        if (transaction.getSenderUser().equals(groupUser.getUser())) {
                            userNetAmountMap.merge(groupUser.getUser(), transaction.getTransactionAmount().negate(), BigDecimal::add);
                        }
                        if (transaction.getRecipientUser().equals(groupUser.getUser())) {
                            userNetAmountMap.merge(groupUser.getUser(), transaction.getTransactionAmount(), BigDecimal::add);
                        }
                    }
                }
            }
        }

        logger.debug("User Net Amount Map: {}", userNetAmountMap);

        List<NetDto> netDtoList = new ArrayList<>();
        for (Map.Entry<UserEntity, BigDecimal> entry : userNetAmountMap.entrySet()) {
            netDtoList.add(new NetDto(entry.getKey(), group.getGroup(), entry.getValue()));
        }
        logger.debug("Net DTO List: {}", netDtoList);
        return netDtoList;
    }

    @Override
    public boolean isReceiptRegisteredInGroup(Long groupId) {
        List<ReceiptEntity> receipts = receiptRepo.findReceiptByGroupId(groupId);
        return !receipts.isEmpty();
    }
}
