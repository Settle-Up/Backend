package settleup.backend.domain.receipt.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptDto {
    private String receiptId;
    private String receiptName;
    private String address;
    private String receiptDate;
    private String groupId;
    private String groupName;
    private String payerUserId;
    private String payerUserName;
    private String allocationType;
    private String totalPrice;
    private String discountApplied;
    private String actualPaidPrice;
    private String createdAt;
    private List<ReceiptItemDto> receiptItemList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReceiptItemDto {
        private String receiptItemName;
        private String totalItemQuantity;
        private String unitPrice;
        private String jointPurchaserCount;
        private List<JointPurchaserDto> jointPurchaserList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JointPurchaserDto {
        private String userId;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String userName;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String purchasedQuantity;
    }
}
