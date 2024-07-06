package settleup.backend.domain.transaction.service.Impl;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupUserTypeEntity;
import settleup.backend.domain.group.repository.GroupUserBaseRepository;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.repository.RequireTransactionRepository;
import settleup.backend.domain.transaction.service.NetService;
import settleup.backend.domain.user.entity.UserTypeEntity;
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
        GroupUserBaseRepository<? extends GroupUserTypeEntity> groupUserRepo = selector.getGroupUserRepository(isUserType);

        List<? extends GroupUserTypeEntity> groupUsers = groupUserRepo.findByGroup_Id(group.getGroup().getId());
        List<RequiresTransactionEntity> netTargetList = transactionRepo.findByGroupIdAndRequiredReflection(group.getGroup().getId());
        Map<UserTypeEntity, BigDecimal> userNetAmountMap = new HashMap<>();

        logger.debug("Group Users: {}", groupUsers);
        logger.debug("Net Target List: {}", netTargetList);

        if (netTargetList.isEmpty() && isReceiptRegisteredInGroup(group.getGroup().getId())) {
            for (GroupUserTypeEntity groupUser : groupUsers) {
                userNetAmountMap.put(groupUser.getUser(), BigDecimal.ZERO);
            }
        } else {
            for (GroupUserTypeEntity groupUser : groupUsers) {
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
        for (Map.Entry<UserTypeEntity, BigDecimal> entry : userNetAmountMap.entrySet()) {
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
