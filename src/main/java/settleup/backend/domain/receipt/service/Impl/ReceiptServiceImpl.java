package settleup.backend.domain.receipt.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemUserEntity;
import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.domain.receipt.entity.dto.RequireTransactionDto;
import settleup.backend.domain.receipt.repository.ReceiptItemRepository;
import settleup.backend.domain.receipt.repository.ReceiptItemUserRepository;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.receipt.service.ReceiptService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@AllArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepo;
    private final ReceiptItemRepository receiptItemRepo;
    private final ReceiptItemUserRepository receiptItemUserRepo;
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final GroupUserRepository groupUserRepo;
    private final UUID_Helper uuidHelper;

    @Override
    public RequireTransactionDto createReceipt(ReceiptDto requestDto) throws CustomException {
         isCheckValidUser(requestDto);

        GroupEntity groupEntity = groupRepo.findByGroupUUID(requestDto.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        String receiptUUID = uuidHelper.UUIDForReceipt();

        ReceiptEntity receiptEntity = new ReceiptEntity();
        receiptEntity.setReceiptUUID(receiptUUID);
        receiptEntity.setReceiptName(requestDto.getReceiptName());
        receiptEntity.setAddress(requestDto.getAddress());
        receiptEntity.setReceiptDate(requestDto.getReceiptDate());
        receiptEntity.setTotalPrice(Double.valueOf(requestDto.getTotalPrice()));
        receiptEntity.setDiscountApplied(Double.valueOf(requestDto.getDiscountApplied()));
        receiptEntity.setActualPaidPrice(Double.valueOf(requestDto.getActualPaidPrice()));
        receiptEntity.setAllocationType(requestDto.getAllocationType());

        String payerUserUUID = requestDto.getPayerUserId();
        UserEntity payerUser = userRepo.findByUserUUID(payerUserUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        receiptEntity.setPayerUser(payerUser);

        receiptEntity.setCreatedAt(LocalDateTime.now());
        receiptRepo.save(receiptEntity);

        requestDto.getReceiptItemList().forEach(itemDto -> {
            ReceiptItemEntity itemEntity = new ReceiptItemEntity();
            itemEntity.setReceiptItemName(itemDto.getReceiptItemName());
            itemEntity.setItemQuantity(Double.valueOf(itemDto.getTotalItemQuantity()));
            itemEntity.setItemPrice(Double.valueOf(itemDto.getUnitPrice()));
            itemEntity.setEngagerCount(Integer.parseInt(itemDto.getJointPurchaserCount()));

            itemEntity.setReceipt(receiptEntity);

            ReceiptItemEntity savedItemEntity = receiptItemRepo.save(itemEntity);

            itemDto.getJointPurchaserList().forEach(jointPurchaserDto -> {
                ReceiptItemUserEntity itemUserEntity = new ReceiptItemUserEntity();
                itemUserEntity.setReceiptItem(savedItemEntity);

                String itemQuantityStr = jointPurchaserDto.getPurchasedQuantity();
                Double itemQuantity = null;
                if (itemQuantityStr != null) {
                    try {
                        itemQuantity = Double.parseDouble(itemQuantityStr.trim());
                    } catch (NumberFormatException e) {
                        throw e;
                    }
                }
                itemUserEntity.setPurchasedQuantity(itemQuantity);
                UserEntity owedUser = userRepo.findByUserUUID(jointPurchaserDto.getUserId())
                        .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
                itemUserEntity.setUser(owedUser);

                receiptItemUserRepo.save(itemUserEntity);
            });

        });
        RequireTransactionDto transactionDto =new RequireTransactionDto();
        transactionDto.setReceipt(receiptEntity);
        transactionDto.setGroup(groupEntity); //
        transactionDto.setAllocationType(requestDto.getAllocationType());
        transactionDto.setPayerUser(receiptEntity.getPayerUser());
        return transactionDto;
    }


    private boolean isCheckValidUser(ReceiptDto requestDto) {
        groupRepo.findByGroupUUID(requestDto.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        UserEntity user = userRepo.findByUserUUID(requestDto.getPayerUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<GroupUserEntity> memberships = groupUserRepo.findByUserId(user.getId());
        return !memberships.isEmpty();
    }

    @Override
    public ReceiptDto getReceiptInfo(String receiptUUID) throws CustomException {
        ReceiptEntity existingReceipt = receiptRepo.findByReceiptUUID(receiptUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.RECEIPT_NOT_FOUND));

        ReceiptItemUserEntity existingGroup =

        ReceiptDto receiptDto = new ReceiptDto();
        receiptDto.setReceiptId(existingReceipt.getReceiptUUID());
        receiptDto.setReceiptName(existingReceipt.getReceiptName());
        receiptDto.setAddress(existingReceipt.getAddress());
        receiptDto.setReceiptDate(existingReceipt.getReceiptDate().toString()); // LocalDate 또는 LocalDateTime을 가정
        receiptDto.setGroupId(existingReceipt.get().getId().toString()); // GroupEntity를 가정
        receiptDto.setGroupName(existingReceipt.getGroup().getGroupName()); // GroupEntity를 가정
        receiptDto.setPayerUserId(existingReceipt.getPayerUser().getId().toString()); // UserEntity를 가정
        receiptDto.setPayerUserName(existingReceipt.getPayerUser().getUserName()); // UserEntity를 가정
        receiptDto.setAllocationType(existingReceipt.getAllocationType());
        receiptDto.setTotalPrice(existingReceipt.getTotalPrice().toString());
        receiptDto.setDiscountApplied(existingReceipt.getDiscountApplied().toString());
        receiptDto.setActualPaidPrice(existingReceipt.getActualPaidPrice().toString());

        // receiptItemList 채우기 (예시 코드)
        List<ReceiptItemDto> receiptItemList = existingReceipt.getReceiptItems() // ReceiptEntity의 receiptItems 컬렉션을 가정
                .stream()
                .map(item -> {
                    ReceiptItemDto itemDto = new ReceiptItemDto();
                    itemDto.setReceiptItemName(item.getReceiptItemName());
                    itemDto.setTotalItemQuantity(item.getItemQuantity().toString());
                    itemDto.setUnitPrice(item.getItemPrice().toString());
                    itemDto.setJointPurchaserCount(item.getEngagerCount().toString());

                    // jointPurchaserList 채우기 (예시 코드)
                    List<JointPurchaserDto> jointPurchaserList = item.getReceiptItemUsers() // ReceiptItemEntity의 receiptItemUsers 컬렉션을 가정
                            .stream()
                            .map(purchaser -> new JointPurchaserDto(
                                    purchaser.getUser().getId().toString(), // UserEntity를 가정
                                    purchaser.getPurchasedQuantity().toString()
                            ))
                            .collect(Collectors.toList());

                    itemDto.setJointPurchaserList(jointPurchaserList);
                    return itemDto;
                })
                .collect(Collectors.toList());

        receiptDto.setReceiptItemList(receiptItemList);

        return receiptDto;
    }
}
