package settleup.backend.domain.group.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupOverviewDto {
    private String userId;
    private String userName;
    private String groupId;
    private String groupName;
    private boolean isMonthlyReportUpdateOn;
    private String settlementBalance;
    private List <OverviewTransactionDto> neededTransactionList;
    private List <OverviewTransactionDto> lastWeekSettledTransactionList;
    private List <ExpenseDto> expenseList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OverviewTransactionDto {
        private String counterPartyId;
        private String counterPartyName;
        private String transactionAmount;
        private String transactionId;
        private String transactionDirection;
        private boolean hasSentOrReceived;
        private String isReject;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExpenseDto {
        private String payerUserId;
        private String payerUserName;
        private String receiptId;
        private String receiptName;
        private String totalAmount;
        private String userOwedAmount;
    }

}
