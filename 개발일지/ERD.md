### SettleUp ERD

Below is the entity-relationship diagram (ERD) for the Settle_Up Project, illustrating the relationships between users, groups, receipts, transactions, and other entities within the system.

아래는 Settle_Up 프로젝트의 엔티티 관계 다이어그램(ERD)입니다. 사용자, 그룹, 영수증, 거래 등 시스템 내의 다양한 엔티티 간의 관계를 보여줍니다.


```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#ff0000', 'edgeLabelBackground':'#ffffff' }}}%%

classDiagram
    class AbstractGroupEntity {
        Long id
        String groupUUID
        String groupName
        String groupUrl
        LocalDateTime createdAt
        Status groupType
    }

    class AbstractGroupUserEntity {
        Long id
        Boolean isMonthlyReportUpdateOn
        AbstractUserEntity user
        AbstractGroupEntity group
        Status groupUserType
    }

    class DemoGroupEntity {
        Status groupType = "DEMO"
    }
    AbstractGroupEntity <|-- DemoGroupEntity

    class DemoGroupUserEntity {
        Status groupUserType = "DEMO"
    }
    AbstractGroupUserEntity <|-- DemoGroupUserEntity

    class GroupEntity {
        Status groupType = "REGULAR"
    }
    AbstractGroupEntity <|-- GroupEntity

    class GroupUserEntity {
        Status groupUserType = "REGULAR"
    }
    AbstractGroupUserEntity <|-- GroupUserEntity

    class AbstractUserEntity {
        Long id
        String userUUID
        String userName
        String userPhone
        String userEmail
        Boolean isDecimalInputOption
        Status userType
    }

    class DemoUserEntity {
        LocalDateTime createdAt
        String ip
        Boolean isDummy
        Status userType = "DEMO"
    }
    AbstractUserEntity <|-- DemoUserEntity

    class UserEntity {
        Status userType = "REGULAR"
    }
    AbstractUserEntity <|-- UserEntity

    class ReceiptEntity {
        Long id
        String receiptUUID
        String receiptName
        AbstractGroupEntity group
        String address
        LocalDate receiptDate
        AbstractUserEntity payerUser
        BigDecimal totalPrice
        BigDecimal discountApplied
        BigDecimal actualPaidPrice
        String allocationType
        LocalDateTime createdAt
        Status userType
    }

    class ReceiptItemEntity {
        Long id
        ReceiptEntity receipt
        String receiptItemName
        BigDecimal itemQuantity
        BigDecimal unitPrice
        Integer jointPurchaserCount
    }

    class ReceiptItemUserEntity {
        Long id
        ReceiptItemEntity receiptItem
        BigDecimal purchasedQuantity
        BigDecimal purchasedTotalAmount
        AbstractUserEntity user
        Status userType
    }

    class RequiresTransactionEntity {
        Long id
        String transactionUUID
        ReceiptEntity receipt
        AbstractGroupEntity group
        AbstractUserEntity senderUser
        AbstractUserEntity recipientUser
        LocalDateTime createdAt
        Status userType
        BigDecimal transactionAmount
        Status requiredReflection
        LocalDateTime clearStatusTimestamp
    }

    class OptimizedTransactionEntity {
        Long id
        String transactionUUID
        AbstractGroupEntity group
        AbstractUserEntity senderUser
        AbstractUserEntity recipientUser
        BigDecimal transactionAmount
        Status optimizationStatus
        Boolean hasBeenSent
        Boolean hasBeenChecked
        Status requiredReflection
        LocalDateTime clearStatusTimestamp
        LocalDateTime createdAt
        Status userType
    }

    class OptimizedTransactionDetailsEntity {
        Long id
        String transactionDetailUUID
        OptimizedTransactionEntity optimizedTransaction
        RequiresTransactionEntity requiresTransaction
    }

    class GroupOptimizedTransactionEntity {
        Long id
        String transactionUUID
        AbstractGroupEntity group
        AbstractUserEntity senderUser
        AbstractUserEntity recipientUser
        BigDecimal transactionAmount
        Status optimizationStatus
        LocalDateTime createdAt
        Boolean hasBeenSent
        Boolean hasBeenChecked
        Status requiredReflection
        LocalDateTime clearStatusTimestamp
        Status userType
    }

    class GroupOptimizedTransactionDetailsEntity {
        Long id
        String transactionDetailUUID
        GroupOptimizedTransactionEntity groupOptimizedTransaction
        OptimizedTransactionEntity optimizedTransaction
    }

    class UltimateOptimizedTransactionEntity {
        Long id
        String transactionUUID
        AbstractGroupEntity group
        AbstractUserEntity senderUser
        AbstractUserEntity recipientUser
        BigDecimal transactionAmount
        Status optimizationStatus
        LocalDateTime createdAt
        Boolean hasBeenSent
        Boolean hasBeenChecked
        Status requiredReflection
        LocalDateTime clearStatusTimestamp
        Status userType
    }

    class UltimateOptimizedTransactionDetailEntity {
        Long id
        String transactionDetailUUID
        UltimateOptimizedTransactionEntity ultimateTransaction
        String usedOptimizedTransactionUUID
    }

    AbstractGroupEntity <|-- DemoGroupEntity
    AbstractGroupEntity <|-- GroupEntity
    AbstractGroupUserEntity <|-- DemoGroupUserEntity
    AbstractGroupUserEntity <|-- GroupUserEntity
    AbstractUserEntity <|-- DemoUserEntity
    AbstractUserEntity <|-- UserEntity
    AbstractGroupEntity <|-- GroupOptimizedTransactionEntity
    AbstractGroupEntity <|-- OptimizedTransactionEntity
    AbstractGroupEntity <|-- UltimateOptimizedTransactionEntity
    AbstractUserEntity <|-- GroupOptimizedTransactionEntity
    AbstractUserEntity <|-- OptimizedTransactionEntity
    AbstractUserEntity <|-- UltimateOptimizedTransactionEntity
    AbstractUserEntity <|-- RequiresTransactionEntity
    AbstractUserEntity <|-- ReceiptEntity
    AbstractUserEntity <|-- ReceiptItemUserEntity
    AbstractUserEntity <|-- AbstractGroupUserEntity
    ReceiptEntity <|-- ReceiptItemEntity
    ReceiptItemEntity <|-- ReceiptItemUserEntity
    ReceiptEntity <|-- RequiresTransactionEntity
    RequiresTransactionEntity <|-- OptimizedTransactionDetailsEntity
    OptimizedTransactionEntity <|-- OptimizedTransactionDetailsEntity
    OptimizedTransactionEntity <|-- GroupOptimizedTransactionDetailsEntity
    GroupOptimizedTransactionEntity <|-- GroupOptimizedTransactionDetailsEntity
    UltimateOptimizedTransactionEntity <|-- UltimateOptimizedTransactionDetailEntity

