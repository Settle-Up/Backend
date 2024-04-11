package settleup.backend.domain.group.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.global.common.Status;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupOverviewDto {
    private String userId;
    private String userName;
    private String groupId;
    private String groupName;
    private Boolean isMonthlyReportUpdateOn;
    private String settlementBalance;
    private List<OverviewTransactionDto> neededTransactionList;
    private List<OverviewTransactionDto> lastWeekSettledTransactionList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OverviewTransactionDto {
        private String counterPartyId;
        private String counterPartyName;
        private String transactionAmount;
        private String transactionId;
        private Status transactionDirection;
        private Boolean hasSentOrReceived;
        private Boolean isRejected;
        private String clearedAt;
    }


}
