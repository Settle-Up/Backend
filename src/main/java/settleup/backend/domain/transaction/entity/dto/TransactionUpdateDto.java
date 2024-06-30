package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionUpdateDto {
    private String userId;
    private String userName;
    private List<TransactionListDto> transactionUpdateList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TransactionListDto{
        private String transactionId;
        private String groupId;
        private String groupName;
        private String counterPartyId;
        private String counterPartyName;
        private String transactionDirection;
        private String transactionAmount;
        private String clearedAt;
    }
}
