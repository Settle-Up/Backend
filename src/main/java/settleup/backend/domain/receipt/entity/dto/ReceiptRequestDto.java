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
    private String groupId; // 유효성 대상
    private String groupName;
    private String payerUserId;  // 유효성 대상
    private String payerUserName;
    private String allocationType; // controller 둘중 한개 여야함
    private String totalPrice; // controller 에서  0 일 경우 에러 반환 ,receiptItemList 들 안에 들은 totalItemQuantity * unitPrice 가 토탈 아닐 경우 에러 반환
    private String discountApplied;
    private String actualPaidPrice;
    private List<ReceiptItemDto> receiptItemList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReceiptItemDto {
        private String receiptItemName;
        private String totalItemQuantity; //controller 에서 0 이거나 숫자가 아닌 다른 값으로 들어올 경우 확인 필
        private String unitPrice; //controller 에서 0 이거나 숫자가 아닌 다른 값으로 들어올 경우 확인 필
        private String jointPurchaserCount; // controller 에서 0 이거나 숫자가 아닌 다른 값으로 들어올 경우 확인 필
        private List<JointPurchaserDto> jointPurchaserList;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JointPurchaserDto {
        private String userId; // 유효성 대상
        private String itemQuantity; //nullable
    }
}
