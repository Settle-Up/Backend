package settleup.backend.domain.user.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.repository.AbstractGroupUserRepository;
import settleup.backend.domain.group.repository.DemoGroupRepository;
import settleup.backend.domain.group.repository.DemoGroupUserRepository;
import settleup.backend.domain.receipt.repository.ReceiptItemRepository;
import settleup.backend.domain.receipt.repository.ReceiptItemUserRepository;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.transaction.repository.*;
import settleup.backend.domain.user.repository.DemoUserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletionService {

    private final AbstractGroupUserRepository abstractGroupUserRepository;
    private final DemoGroupUserRepository demoGroupUserRepository;
    private final ReceiptItemUserRepository receiptItemUserRepository;
    private final ReceiptItemRepository receiptItemRepository;
    private final ReceiptRepository receiptRepository;
    private final GroupOptimizedTransactionRepository groupOptimizedTransactionRepository;
    private final GroupOptimizedTransactionDetailRepository groupOptimizedTransactionDetailsRepository;
    private final OptimizedTransactionRepository optimizedTransactionRepository;
    private final OptimizedTransactionDetailsRepository optimizedTransactionDetailsRepository;
    private final RequireTransactionRepository requiresTransactionRepository;
    private final UltimateOptimizedTransactionRepository ultimateOptimizedTransactionRepository;
    private final UltimateOptimizedTransactionDetailRepository ultimateOptimizedTransactionDetailsRepository;
    private final JdbcTemplate jdbcTemplate;
    private final DemoUserRepository demoUserRepository;
    private final DemoGroupRepository demoGroupRepository;

    @Transactional
    public void deleteUserWithRelatedEntities(Long userId) {
        log.info("Starting deletion of related entities for user ID: {}", userId);

        try {
            // Step 1: Disable foreign key checks
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

            // Step 2: Delete from receipt_item_user
            log.info("Deleting from receipt_item_user for user ID: {}", userId);
            receiptItemUserRepository.deleteByUserId(userId);

            // Step 3: Delete from settle_demo_group_user
            log.info("Deleting from settle_demo_group_user for user ID: {}", userId);
            demoGroupUserRepository.deleteByUserId(userId);

            // Step 4: Delete from abstract_group_user
            log.info("Deleting from abstract_group_user for user ID: {}", userId);
            abstractGroupUserRepository.deleteByUserId(userId);

            // Step 5: Get receipt item IDs for the user
            List<Long> receiptItemIds = receiptItemRepository.findIdsByReceiptPayerUserId(userId);

            // Step 6: Delete related entries from receipt_item_user by receipt item IDs
            if (!receiptItemIds.isEmpty()) {
                log.info("Deleting from ReceiptItemUserRepository for receipt item IDs: {}", receiptItemIds);
                receiptItemUserRepository.deleteByReceiptItem_IdIn(receiptItemIds);
            }

            // Step 7: Delete from receipt_item
            log.info("Deleting from ReceiptItemRepository for user ID: {}", userId);
            receiptItemRepository.deleteByReceiptPayerUserId(userId);

            // Step 8: Delete from receipt
            log.info("Deleting from ReceiptRepository for payer user ID: {}", userId);
            receiptRepository.deleteByPayerUserId(userId);

            // Step 9: Additional steps to delete other related entities...
            log.info("Deleting from OptimizedTransactionDetailsRepository for sender user ID: {}", userId);
            optimizedTransactionDetailsRepository.deleteByOptimizedTransaction_SenderUser_Id(userId);

            log.info("Deleting from OptimizedTransactionDetailsRepository for recipient user ID: {}", userId);
            optimizedTransactionDetailsRepository.deleteByOptimizedTransaction_RecipientUser_Id(userId);

            log.info("Deleting from GroupOptimizedTransactionDetailsRepository for sender user ID: {}", userId);
            groupOptimizedTransactionDetailsRepository.deleteByGroupOptimizedTransaction_SenderUser_Id(userId);

            log.info("Deleting from GroupOptimizedTransactionDetailsRepository for recipient user ID: {}", userId);
            groupOptimizedTransactionDetailsRepository.deleteByGroupOptimizedTransaction_RecipientUser_Id(userId);

            log.info("Deleting from UltimateOptimizedTransactionDetailsRepository for sender user ID: {}", userId);
            ultimateOptimizedTransactionDetailsRepository.deleteByUltimateOptimizedTransaction_SenderUser_Id(userId);

            log.info("Deleting from UltimateOptimizedTransactionDetailsRepository for recipient user ID: {}", userId);
            ultimateOptimizedTransactionDetailsRepository.deleteByUltimateOptimizedTransaction_RecipientUser_Id(userId);

            log.info("Deleting from AbstractGroupUserRepository for user ID: {}", userId);
            abstractGroupUserRepository.deleteByUserId(userId);

            log.info("Deleting from GroupOptimizedTransactionRepository for sender user ID: {}", userId);
            groupOptimizedTransactionRepository.deleteBySenderUserId(userId);

            log.info("Deleting from GroupOptimizedTransactionRepository for recipient user ID: {}", userId);
            groupOptimizedTransactionRepository.deleteByRecipientUserId(userId);

            log.info("Deleting from OptimizedTransactionRepository for sender user ID: {}", userId);
            optimizedTransactionRepository.deleteBySenderUserId(userId);

            log.info("Deleting from OptimizedTransactionRepository for recipient user ID: {}", userId);
            optimizedTransactionRepository.deleteByRecipientUserId(userId);

            log.info("Deleting from RequireTransactionRepository for sender user ID: {}", userId);
            requiresTransactionRepository.deleteBySenderUserId(userId);

            log.info("Deleting from RequireTransactionRepository for recipient user ID: {}", userId);
            requiresTransactionRepository.deleteByRecipientUserId(userId);

            log.info("Deleting from UltimateOptimizedTransactionRepository for sender user ID: {}", userId);
            ultimateOptimizedTransactionRepository.deleteBySenderUserId(userId);

            log.info("Deleting from UltimateOptimizedTransactionRepository for recipient user ID: {}", userId);
            ultimateOptimizedTransactionRepository.deleteByRecipientUserId(userId);

            // Step 10: Delete from abstract_user
            log.info("Deleting from abstract_user for user ID: {}", userId);
            demoUserRepository.deleteById(userId);

            log.info("Successfully deleted all related entities for user ID: {}", userId);
        } catch (Exception e) {
            log.error("Failed to delete related entities for user ID: {}", userId, e);
            throw e;
        } finally {
            // Step 11: Re-enable foreign key checks
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }

    @Transactional
    public void deleteExpiredGroups(LocalDateTime expirationTime) {
        log.info("Deleting expired demo groups...");

        // Disable foreign key checks
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        try {
            // Find and delete expired groups
            List<Long> expiredGroupIds = demoGroupRepository.findExpiredGroupIds(expirationTime);
            for (Long groupId : expiredGroupIds) {
                log.info("Deleting from settle_demo_group_user for group ID: {}", groupId);
                demoGroupUserRepository.deleteByGroupId(groupId);

                log.info("Deleting from receipt_item_user for group ID: {}", groupId);
                receiptItemUserRepository.deleteByGroupId(groupId);

                log.info("Deleting from receipt_item for group ID: {}", groupId);
                receiptItemRepository.deleteByGroupId(groupId);

                log.info("Deleting from receipt for group ID: {}", groupId);
                receiptRepository.deleteByGroupId(groupId);

                log.info("Deleting from demo_group for group ID: {}", groupId);
                demoGroupRepository.deleteById(groupId);

                log.info("Deleting from abstract_group for group ID: {}", groupId);
                demoGroupRepository.deleteAbstractGroupById(groupId);

                log.info("Successfully deleted group with ID: {}", groupId);
            }

            log.info("Successfully deleted all expired groups.");
        } catch (Exception e) {
            log.error("Failed to delete expired groups.", e);
        } finally {
            // Re-enable foreign key checks
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
        }
    }
}
