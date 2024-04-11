package settleup.backend.domain.group.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupOverviewExpenseDto {
    private Boolean hasNextPage;
    private List<ExpenseDto> expenses;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ExpenseDto {
        private String payerUserId;
        private String payerUserName;
        private String receiptId;
        private String receiptName;
        private String totalPrice;
        private String userOwedAmount;
        private String createdAt;
    }
}
