package settleup.backend.domain.receipt.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
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
import settleup.backend.domain.user.entity.UserTypeEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.Helper.Status;
import settleup.backend.global.Helper.UUID_Helper;
import settleup.backend.global.Selector.UserRepoSelector;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@AllArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepo;
    private final ReceiptItemRepository receiptItemRepo;
    private final ReceiptItemUserRepository receiptItemUserRepo;
    private final UUID_Helper uuidHelper;
    private final ApplicationEventPublisher eventPublisher;
    private final UserRepoSelector selector;

        @Override
        @Transactional
        public TransactionDto createReceipt(ReceiptDto requestDto, Boolean isUserType) {

            GroupTypeEntity groupEntity = selector.getGroupRepository(isUserType).findByGroupUUID(requestDto.getGroupId())
                    .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

            UserTypeEntity userEntity = selector.getUserRepository(isUserType).findByUserUUID(requestDto.getPayerUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.PAYER_USER_NOT_FOUND));

            boolean existsInGroup = selector.getGroupUserRepository(isUserType).existsByUserId(userEntity.getId());
            if (!existsInGroup) {
                throw new CustomException(ErrorCode.GROUP_USER_NOT_FOUND);
            }

            isValidTotalAmount(requestDto);

            ReceiptEntity receiptEntity = new ReceiptEntity();
            String receiptUUID = uuidHelper.UUIDForReceipt();
            receiptEntity.setReceiptUUID(receiptUUID);
            receiptEntity.setReceiptName(requestDto.getReceiptName());
            receiptEntity.setAddress(requestDto.getAddress());
            receiptEntity.setGroup((GroupEntity) groupEntity); // Casting 올바른지 확인
            receiptEntity.setReceiptDate(requestDto.getReceiptDate());
            receiptEntity.setTotalPrice(new BigDecimal(requestDto.getTotalPrice()));
            receiptEntity.setDiscountApplied(new BigDecimal(requestDto.getDiscountApplied()));
            receiptEntity.setActualPaidPrice(new BigDecimal(requestDto.getActualPaidPrice()));
            receiptEntity.setAllocationType(requestDto.getAllocationType());
            receiptEntity.setPayerUser((UserEntity) userEntity); // Casting 올바른지 확인
            receiptEntity.setCreatedAt(LocalDateTime.now());
            receiptEntity.setUserType(isUserType ? Status.REGULAR : Status.DEMO);

            receiptRepo.save(receiptEntity);

            Set<UserTypeEntity> owedUsers = new HashSet<>();
            requestDto.getReceiptItemList().forEach(itemDto -> {
                ReceiptItemEntity itemEntity = new ReceiptItemEntity();
                itemEntity.setReceiptItemName(itemDto.getReceiptItemName());
                itemEntity.setItemQuantity(new BigDecimal(itemDto.getTotalItemQuantity()));
                itemEntity.setUnitPrice(new BigDecimal(itemDto.getUnitPrice()));
                itemEntity.setJointPurchaserCount(Integer.parseInt(itemDto.getJointPurchaserCount()));
                itemEntity.setReceipt(receiptEntity);

                ReceiptItemEntity savedItemEntity = receiptItemRepo.save(itemEntity);

                int jointPurchaserCount = Integer.parseInt(itemDto.getJointPurchaserCount());
                BigDecimal totalItemQuantity = new BigDecimal(itemDto.getTotalItemQuantity());
                BigDecimal defaultItemQuantityPerUser = totalItemQuantity.divide(new BigDecimal(jointPurchaserCount), 2, BigDecimal.ROUND_HALF_UP);

                itemDto.getJointPurchaserList().forEach(jointPurchaserDto -> {
                    ReceiptItemUserEntity itemUserEntity = new ReceiptItemUserEntity();
                    itemUserEntity.setReceiptItem(savedItemEntity);

                    BigDecimal itemQuantity = null;
                    if (jointPurchaserDto.getPurchasedQuantity() != null) {
                        try {
                            itemQuantity = new BigDecimal(jointPurchaserDto.getPurchasedQuantity().trim());
                        } catch (NumberFormatException e) {
                            throw new CustomException(ErrorCode.INVALID_INPUT, "Invalid purchased quantity format");
                        }
                    } else {
                        itemQuantity = defaultItemQuantityPerUser;
                    }
                    itemUserEntity.setPurchasedQuantity(itemQuantity);

                    UserEntity owedUser = (UserEntity) selector.getUserRepository(isUserType).findByUserUUID(jointPurchaserDto.getUserId())
                            .orElseThrow(() -> new CustomException(ErrorCode.OWED_USER_NOT_FOUND)); // Casting
                    itemUserEntity.setUser(owedUser);

                    receiptItemUserRepo.save(itemUserEntity);
                });
            });

            TransactionDto transactionDto = new TransactionDto();
            transactionDto.setReceipt(receiptEntity);
            transactionDto.setGroup(groupEntity); // Casting
            transactionDto.setAllocationType(requestDto.getAllocationType());
            transactionDto.setPayerUser(userEntity); // Casting
            transactionDto.setOwedUser(new ArrayList<>(owedUsers));
            return transactionDto;
        }

        private void isValidTotalAmount(ReceiptDto requestDto) throws CustomException {
            BigDecimal expectedTotalPrice = new BigDecimal(requestDto.getActualPaidPrice());
            BigDecimal calculatedSimpleTotalPrice = requestDto.getReceiptItemList().stream()
                    .map(item -> new BigDecimal(item.getUnitPrice()).multiply(new BigDecimal(item.getTotalItemQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal calculatedTotalPriceByEachUser = BigDecimal.ZERO;

            for (ReceiptDto.ReceiptItemDto item : requestDto.getReceiptItemList()) {
                BigDecimal itemPrice = new BigDecimal(item.getUnitPrice());
                BigDecimal totalItemQuantity = new BigDecimal(item.getTotalItemQuantity());
                int jointPurchaserCount = Integer.parseInt(item.getJointPurchaserCount());

                for (ReceiptDto.JointPurchaserDto itemUser : item.getJointPurchaserList()) {
                    BigDecimal saveAmount;
                    if (itemUser.getPurchasedQuantity() != null) {
                        BigDecimal itemQuantityEachPerson = new BigDecimal(itemUser.getPurchasedQuantity());
                        saveAmount = itemPrice.multiply(itemQuantityEachPerson);
                    } else {
                        saveAmount = itemPrice.multiply(totalItemQuantity).divide(new BigDecimal(jointPurchaserCount), 2, BigDecimal.ROUND_HALF_UP);
                    }
                    calculatedTotalPriceByEachUser = calculatedTotalPriceByEachUser.add(saveAmount);
                }
            }

            if (calculatedSimpleTotalPrice.subtract(expectedTotalPrice).abs().compareTo(new BigDecimal("0.1")) > 0) {
                throw new CustomException(ErrorCode.TOTAL_AMOUNT_ERROR, "Simply calculated total amount does not match expected.");
            }

            if (calculatedTotalPriceByEachUser.subtract(expectedTotalPrice).abs().compareTo(new BigDecimal("0.1")) > 0) {
                throw new CustomException(ErrorCode.TOTAL_AMOUNT_ERROR, "Each user calculated total amount does not match expected.");
            }
        }

        @Override
        public ReceiptDto getReceiptInfo(UserInfoDto userInfoDto, String receiptUUID) throws CustomException {
            ReceiptEntity existingReceipt = receiptRepo.findByReceiptUUID(receiptUUID)
                    .orElseThrow(() -> new CustomException(ErrorCode.RECEIPT_NOT_FOUND));

            Status userType = existingReceipt.getUserType();
            UserTypeEntity existingUser = selector.getUserRepository(userType == Status.REGULAR).findByUserUUID(userInfoDto.getUserId())
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

            GroupTypeEntity existingGroup = selector.getGroupRepository(userType == Status.REGULAR).findByGroupUUID(existingReceipt.getGroup().getGroupUUID())
                    .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));

            selector.getGroupUserRepository(userType == Status.REGULAR).findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));

            ReceiptDto receiptDto = new ReceiptDto();
            receiptDto.setReceiptId(existingReceipt.getReceiptUUID());
            receiptDto.setReceiptName(existingReceipt.getReceiptName());
            receiptDto.setAddress(existingReceipt.getAddress());
            receiptDto.setReceiptDate(existingReceipt.getReceiptDate().toString());
            receiptDto.setGroupId(existingGroup.getGroupUUID());
            receiptDto.setGroupName(existingGroup.getGroupName());
            receiptDto.setPayerUserId(existingReceipt.getPayerUser().getUserUUID());
            receiptDto.setPayerUserName(existingReceipt.getPayerUser().getUserName());
            receiptDto.setAllocationType(existingReceipt.getAllocationType());
            receiptDto.setTotalPrice(String.format("%.2f", existingReceipt.getTotalPrice()));
            receiptDto.setDiscountApplied(String.format("%.2f", existingReceipt.getDiscountApplied()));
            receiptDto.setActualPaidPrice(String.format("%.2f", existingReceipt.getActualPaidPrice()));
            receiptDto.setCreatedAt(String.valueOf(existingReceipt.getCreatedAt()));

            List<ReceiptItemEntity> receiptItems = receiptItemRepo.findByReceiptId(existingReceipt.getId());

            List<ReceiptDto.ReceiptItemDto> receiptItemList = new ArrayList<>();
            for (ReceiptItemEntity item : receiptItems) {
                ReceiptDto.ReceiptItemDto itemDto = new ReceiptDto.ReceiptItemDto();
                itemDto.setReceiptItemName(item.getReceiptItemName());
                itemDto.setTotalItemQuantity(String.format("%.2f", item.getItemQuantity()));
                itemDto.setUnitPrice(String.format("%.2f", item.getUnitPrice()));
                itemDto.setJointPurchaserCount(item.getJointPurchaserCount().toString());

                List<ReceiptDto.JointPurchaserDto> jointPurchaserList = new ArrayList<>();
                for (ReceiptItemUserEntity purchaser : receiptItemUserRepo.findByReceiptItemId(item.getId())) {
                    String formattedQuantity = purchaser.getPurchasedQuantity() != null ?
                            String.format("%.2f", purchaser.getPurchasedQuantity()) : null;

                    jointPurchaserList.add(new ReceiptDto.JointPurchaserDto(
                            purchaser.getUser().getUserUUID(),
                            purchaser.getUser().getUserName(),
                            formattedQuantity
                    ));
                }
                itemDto.setJointPurchaserList(jointPurchaserList);
                receiptItemList.add(itemDto);
            }

            receiptDto.setReceiptItemList(receiptItemList);

            return receiptDto;
        }
    }


