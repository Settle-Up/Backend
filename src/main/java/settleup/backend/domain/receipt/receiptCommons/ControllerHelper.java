package settleup.backend.domain.receipt.receiptCommons;

import settleup.backend.domain.receipt.entity.dto.ReceiptDto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ControllerHelper {

    public static String checkRequiredWithFilter(ReceiptDto dto) {
        List<Supplier<String>> checks = new ArrayList<>();

        checks.add(() -> isNullOrEmpty(dto.getReceiptName()) ? "receiptName" : null);
        checks.add(() -> isNullOrEmpty(dto.getAddress()) ? "address" : null);
        checks.add(() -> isNullOrEmpty(dto.getGroupId()) ? "groupId" : null);
        checks.add(() -> isNullOrEmpty(dto.getGroupName()) ? "groupName" : null);
        checks.add(() -> isNullOrEmpty(dto.getPayerUserId()) ? "payerUserId" : null);
        checks.add(() -> isNullOrEmpty(dto.getPayerUserName()) ? "payerUserName" : null);
        checks.add(() -> isNullOrEmpty(dto.getAllocationType()) ? "allocationType" : null);
        checks.add(() -> isNullOrEmpty(dto.getTotalPrice()) ? "totalPrice" : null);
        checks.add(() -> isNullOrEmpty(dto.getDiscountApplied()) ? "discountApplied" : null);
        checks.add(() -> isNullOrEmpty(dto.getActualPaidPrice()) ? "actualPaidPrice" : null);


        if (dto.getReceiptItemList() == null || dto.getReceiptItemList().isEmpty()) {
            checks.add(() -> "receiptItemList");
        } else {
            for (int i = 0; i < dto.getReceiptItemList().size(); i++) {
                final int index = i;
                ReceiptDto.ReceiptItemDto item = dto.getReceiptItemList().get(index);

                checks.add(() -> isNullOrEmpty(item.getReceiptItemName()) ? "receiptItemList[" + index + "].receiptItemName" : null);
                checks.add(() -> isNullOrEmpty(item.getTotalItemQuantity()) ? "receiptItemList[" + index + "].totalItemQuantity" : null);
                checks.add(() -> isNullOrEmpty(item.getUnitPrice()) ? "receiptItemList[" + index + "].unitPrice" : null);
                checks.add(() -> isNullOrEmpty(item.getJointPurchaserCount()) ? "receiptItemList[" + index + "].jointPurchaserCount" : null);


                if (item.getJointPurchaserList() == null || item.getJointPurchaserList().isEmpty()) {
                    checks.add(() -> "receiptItemList[" + index + "].jointPurchaserList");
                } else {

                    try {
                        int jointPurchaserCount = Integer.parseInt(item.getJointPurchaserCount());
                        if (jointPurchaserCount != item.getJointPurchaserList().size()) {
                            checks.add(() -> "receiptItemList[" + index + "].jointPurchaserList size mismatch with jointPurchaserCount");
                        }
                    } catch (NumberFormatException e) {
                        checks.add(() -> "receiptItemList[" + index + "].jointPurchaserCount is not a valid number");
                    }

                    for (int j = 0; j < item.getJointPurchaserList().size(); j++) {
                        final int jointIndex = j;
                        checks.add(() -> isNullOrEmpty(item.getJointPurchaserList().get(jointIndex).getUserId()) ? "receiptItemList[" + index + "].jointPurchaserList[" + jointIndex + "].userId" : null);

                    }
                }
            }
        }

        List<String> missingFields = checks.stream()
                .map(Supplier::get)
                .filter(s -> s != null)
                .collect(Collectors.toList());

        return String.join(", ", missingFields);
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
