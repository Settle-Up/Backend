package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUpdateRequestDto {
    private String transactionId;
    private String approvalUser;


}
