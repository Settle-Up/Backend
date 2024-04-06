package settleup.backend.domain.receipt.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemEntity;
import settleup.backend.domain.receipt.entity.ReceiptItemUserEntity;
import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.receipt.repository.ReceiptItemRepository;
import settleup.backend.domain.receipt.repository.ReceiptItemUserRepository;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.receipt.service.ReceiptService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.event.ReceiptCreatedEvent;
import settleup.backend.global.common.UUID_Helper;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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
    private final ApplicationEventPublisher eventPublisher;

    /**
     * createReceipt
     *
     * @param requestDto receipt
     * @return
     * @throws CustomException
     */

    @Override
    public TransactionDto createReceipt(ReceiptDto requestDto) {

        GroupEntity groupEntity = groupRepo.findByGroupUUID(requestDto.getGroupId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        UserEntity userEntity = userRepo.findByUserUUID(requestDto.getPayerUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYER_USER_NOT_FOUND));

        boolean existsInGroup = groupUserRepo.existsByUser_Id(userEntity.getId());
        if (!existsInGroup) {
            throw new CustomException(ErrorCode.GROUP_USER_NOT_FOUND);
        }

        isValidTotalAmount(requestDto);

        ReceiptEntity receiptEntity = new ReceiptEntity();
        String receiptUUID = uuidHelper.UUIDForReceipt();
        receiptEntity.setReceiptUUID(receiptUUID);
        receiptEntity.setReceiptName(requestDto.getReceiptName());
        receiptEntity.setAddress(requestDto.getAddress());
        receiptEntity.setGroup(groupEntity);
        receiptEntity.setReceiptDate(requestDto.getReceiptDate());
        receiptEntity.setTotalPrice(Double.valueOf(requestDto.getTotalPrice()));
        receiptEntity.setDiscountApplied(Double.valueOf(requestDto.getDiscountApplied()));
        receiptEntity.setActualPaidPrice(Double.valueOf(requestDto.getActualPaidPrice()));
        receiptEntity.setAllocationType(requestDto.getAllocationType());
        receiptEntity.setPayerUser(userEntity);
        receiptEntity.setCreatedAt(LocalDateTime.now());

        receiptRepo.save(receiptEntity);

        Set<UserEntity> owedUsers = new HashSet<>();
        requestDto.getReceiptItemList().forEach(itemDto -> {
            ReceiptItemEntity itemEntity = new ReceiptItemEntity();
            itemEntity.setReceiptItemName(itemDto.getReceiptItemName());
            itemEntity.setItemQuantity(Double.valueOf(itemDto.getTotalItemQuantity()));
            itemEntity.setUnitPrice(Double.valueOf(itemDto.getUnitPrice()));
            itemEntity.setJointPurchaserCount(Integer.parseInt(itemDto.getJointPurchaserCount()));

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
                        .orElseThrow(() -> new CustomException(ErrorCode.OWED_USER_NOT_FOUND));
                itemUserEntity.setUser(owedUser);

                receiptItemUserRepo.save(itemUserEntity);
            });
        });

        TransactionDto transactionDto = new TransactionDto();
        transactionDto.setReceipt(receiptEntity);
        transactionDto.setGroup(groupEntity);
        transactionDto.setAllocationType(requestDto.getAllocationType());
        transactionDto.setPayerUser(receiptEntity.getPayerUser());
        transactionDto.setOwedUser(new ArrayList<>(owedUsers));
        eventPublisher.publishEvent(new ReceiptCreatedEvent(this, transactionDto));
        return transactionDto;
    }


    private void isValidTotalAmount(ReceiptDto requestDto) throws CustomException {
        double expectedTotalPrice = Double.parseDouble(requestDto.getActualPaidPrice());
        double calculatedSimpleTotalPrice = requestDto.getReceiptItemList().stream()
                .mapToDouble(item -> Double.parseDouble(item.getUnitPrice()) * Double.parseDouble(item.getTotalItemQuantity()))
                .sum();

        double calculatedTotalPriceByEachUser = 0;

        for (ReceiptDto.ReceiptItemDto item : requestDto.getReceiptItemList()) {
            double itemPrice = Double.parseDouble(item.getUnitPrice());
            double totalItemQuantity = Double.parseDouble(item.getTotalItemQuantity());
            int jointPurchaserCount = Integer.parseInt(item.getJointPurchaserCount());

            for (ReceiptDto.JointPurchaserDto itemUser : item.getJointPurchaserList()) {
                double saveAmount;
                if (itemUser.getPurchasedQuantity() != null) {
                    double itemQuantityEachPerson = Double.parseDouble(itemUser.getPurchasedQuantity());
                    saveAmount = (itemPrice * totalItemQuantity) * itemQuantityEachPerson / totalItemQuantity;
                } else {
                    saveAmount = (itemPrice * totalItemQuantity) / jointPurchaserCount;
                }
                calculatedTotalPriceByEachUser += saveAmount;
            }
        }

        if (Math.abs(calculatedSimpleTotalPrice - expectedTotalPrice) > 0.001) {
            throw new CustomException(ErrorCode.TOTAL_AMOUNT_ERROR, "Simply calculated total amount does not match expected.");
        }

        if (Math.abs(calculatedTotalPriceByEachUser - expectedTotalPrice) > 0.001) {
            throw new CustomException(ErrorCode.TOTAL_AMOUNT_ERROR, "Each user calculated total amount does not match expected.");
        }
    }

    @Override
    public ReceiptDto getReceiptInfo(UserInfoDto userInfoDto, String receiptUUID) throws CustomException {
        ReceiptEntity existingReceipt = receiptRepo.findByReceiptUUID(receiptUUID)
                .orElseThrow(() -> new CustomException(ErrorCode.RECEIPT_NOT_FOUND));

        UserEntity existingUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        GroupEntity existingGroup = groupRepo.findByGroupUUID(receiptRepo.findByReceiptUUID(receiptUUID).get().getGroup().getGroupUUID())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

        groupUserRepo.findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));

        receiptRepo.findByReceiptUUID(receiptUUID);
        ReceiptDto receiptDto = new ReceiptDto();
        receiptDto.setReceiptId(existingReceipt.getReceiptUUID());
        receiptDto.setReceiptName(existingReceipt.getReceiptName());
        receiptDto.setAddress(existingReceipt.getAddress());
        receiptDto.setReceiptDate(existingReceipt.getReceiptDate().toString());
        receiptDto.setGroupId(existingGroup.getGroupUUID().toString());
        receiptDto.setGroupName(existingReceipt.getGroup().getGroupName());
        receiptDto.setPayerUserId(existingReceipt.getPayerUser().getUserUUID().toString());
        receiptDto.setPayerUserName(existingReceipt.getPayerUser().getUserName());
        receiptDto.setAllocationType(existingReceipt.getAllocationType());
        receiptDto.setTotalPrice(String.format("%.0f", existingReceipt.getTotalPrice()));
        receiptDto.setDiscountApplied(String.format("%.0f", existingReceipt.getDiscountApplied()));
        receiptDto.setActualPaidPrice(String.format("%.0f", existingReceipt.getActualPaidPrice()));

        List<ReceiptItemEntity> receiptItems = receiptRepo.findItemsByReceiptUUID(receiptUUID);

        List<ReceiptDto.ReceiptItemDto> receiptItemList = receiptItems.stream()
                .map(item -> {
                    ReceiptDto.ReceiptItemDto itemDto = new ReceiptDto.ReceiptItemDto();
                    itemDto.setReceiptItemName(item.getReceiptItemName());
                    itemDto.setTotalItemQuantity(String.format("%.0f", item.getItemQuantity()));
                    itemDto.setUnitPrice(String.format("%.0f", item.getUnitPrice()));
                    itemDto.setJointPurchaserCount(item.getJointPurchaserCount().toString());


                    List<ReceiptDto.JointPurchaserDto> jointPurchaserList = receiptItemUserRepo.findByReceiptItemId(item.getId())
                            .stream()
                            .map(purchaser -> {
                                String formattedQuantity = purchaser.getPurchasedQuantity() != null ?
                                        String.format("%.0f", purchaser.getPurchasedQuantity()) : null;

                                return new ReceiptDto.JointPurchaserDto(
                                        purchaser.getUser().getUserUUID().toString(),
                                        purchaser.getUser().getUserName(),
                                        formattedQuantity);
                            })
                            .collect(Collectors.toList());

                    itemDto.setJointPurchaserList(jointPurchaserList);
                    return itemDto;
                }).collect(Collectors.toList());

        receiptDto.setReceiptItemList(receiptItemList);

        return receiptDto;
    }
}