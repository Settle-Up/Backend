package settleup.backend.domain.receipt.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiptRequestDto {
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
        private String purchasedQuantity;
    }
}
