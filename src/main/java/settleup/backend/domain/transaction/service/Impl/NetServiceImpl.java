package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.AbstractGroupUserEntity;
import settleup.backend.domain.group.repository.GroupUserBaseRepository;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.NetService;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.domain.user.entity.dto.UserGroupDto;
import settleup.backend.global.Selector.UserRepoSelector;

import java.math.BigDecimal;
import java.util.*;

@Service
@AllArgsConstructor
@Transactional
public class NetServiceImpl implements NetService {
    private final RequireTransactionRepository transactionRepo;
    private final ReceiptRepository receiptRepo;
    private final UserRepoSelector selector;

    private static final Logger logger = LoggerFactory.getLogger(NetServiceImpl.class);

    @Override
    public List<NetDto> calculateNet(UserGroupDto group) {
        Boolean isUserType = group.getIsUserType();
        GroupUserBaseRepository<? extends AbstractGroupUserEntity> groupUserRepo = selector.getGroupUserRepository(isUserType);

        List<? extends AbstractGroupUserEntity> groupUsers = groupUserRepo.findByGroup_Id(group.getGroup().getId());
        List<RequiresTransactionEntity> netTargetList = transactionRepo.findByGroupIdAndRequiredReflection(group.getGroup().getId());
        Map<AbstractUserEntity, BigDecimal> userNetAmountMap = new HashMap<>();

        logger.debug("Group Users: {}", groupUsers);
        logger.debug("Net Target List: {}", netTargetList);

        if (netTargetList.isEmpty() && isReceiptRegisteredInGroup(group.getGroup().getId())) {
            logger.debug("No transactions found, initializing net amount to zero for all group users.");
            for (AbstractGroupUserEntity groupUser : groupUsers) {
                userNetAmountMap.put(groupUser.getUser(), BigDecimal.ZERO);
            }
        } else {
            for (AbstractGroupUserEntity groupUser : groupUsers) {
                logger.debug("Processing transactions for user: {}", groupUser.getUser().getUserUUID());
                BigDecimal netAmount = BigDecimal.ZERO;
                for (RequiresTransactionEntity transaction : netTargetList) {
                    if (transaction.getSenderUser().equals(groupUser.getUser())) {
                        netAmount = netAmount.subtract(transaction.getTransactionAmount());
                        logger.debug("User {} sent amount {}, net amount: {}", groupUser.getUser().getUserUUID(), transaction.getTransactionAmount(), netAmount);
                    } else if (transaction.getRecipientUser().equals(groupUser.getUser())) {
                        netAmount = netAmount.add(transaction.getTransactionAmount());
                        logger.debug("User {} received amount {}, net amount: {}", groupUser.getUser().getUserUUID(), transaction.getTransactionAmount(), netAmount);
                    }
                }
                userNetAmountMap.put(groupUser.getUser(), netAmount);
            }
        }

        logger.debug("User Net Amount Map: {}", userNetAmountMap);

        List<NetDto> netDtoList = new ArrayList<>();
        for (Map.Entry<AbstractUserEntity, BigDecimal> entry : userNetAmountMap.entrySet()) {
            NetDto netDto = new NetDto(entry.getKey(), group.getGroup(), entry.getValue());
            logger.debug("Created NetDto: {}", netDto);
            netDtoList.add(netDto);
        }
        logger.debug("Final Net DTO List: {}", netDtoList);
        return netDtoList;
    }

    @Override
    public boolean isReceiptRegisteredInGroup(Long groupId) {
        List<ReceiptEntity> receipts = receiptRepo.findReceiptByGroupId(groupId);
        return !receipts.isEmpty();
    }
}
