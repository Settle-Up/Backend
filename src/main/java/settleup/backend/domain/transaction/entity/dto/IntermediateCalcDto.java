package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.user.entity.AbstractUserEntity;


import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntermediateCalcDto {
    private AbstractGroupEntity group;
    private AbstractUserEntity SenderUser;
    private AbstractUserEntity  recipientUser;
    private BigDecimal transactionAmount;
    private List<RequiresTransactionEntity> duringOptimizationUsed;
    private List<TransactionalEntity> duringFinalOptimizationUsed;
}

