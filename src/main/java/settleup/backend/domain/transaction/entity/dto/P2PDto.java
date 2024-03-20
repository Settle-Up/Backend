package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;
import settleup.backend.domain.user.entity.UserEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class P2PDto {
    private GroupEntity group;
    private UserEntity SenderUser;
    private UserEntity recipientUser;
    private double transactionAmount;
    private List<RequiresTransactionEntity> duringOptimizationUsed;
}

