package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import settleup.backend.domain.transaction.entity.RequiresTransactionEntity;

import java.util.List;

@Data
@AllArgsConstructor
public class TransactionP2PCalculationResultDto {
    private double totalAmount;
    private List<RequiresTransactionEntity> allTransactions;
}
