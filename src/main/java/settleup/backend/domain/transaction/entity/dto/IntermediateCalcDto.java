package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.user.entity.UserEntity;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntermediateCalcDto {
    private GroupEntity group;
    private UserEntity SenderUser;
    private UserEntity recipientUser;
    private BigDecimal transactionAmount;
    private List<RequiresTransactionEntity> duringOptimizationUsed;
    private List<TransactionalEntity> duringFinalOptimizationUsed;
}

