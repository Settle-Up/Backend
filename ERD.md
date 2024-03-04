### SettleUp ERD

Below is the entity-relationship diagram (ERD) for the Settle_Up Project, illustrating the relationships between users, groups, receipts, transactions, and other entities within the system.

아래는 Settle_Up 프로젝트의 엔티티 관계 다이어그램(ERD)입니다. 사용자, 그룹, 영수증, 거래 등 시스템 내의 다양한 엔티티 간의 관계를 보여줍니다.

```mermaid
%%{init: {'theme': 'dark', 'themeVariables': { 'primaryColor': '#ff0000', 'edgeLabelBackground':'#ffffff' }}}%%

erDiagram
    USER {
        int id PK
        varchar userName
        varchar userPhone
        varchar userEmail
        varchar userUUID
    }
    GROUP {
        int id PK
        varchar groupName
        varchar groupUrl
        varchar groupUUID
        timestamp createdAt
    }
    GROUPUSER {
        int id PK
        int groupId FK
        int userId FK
        boolean alarmRegistration
    }
    RECEIPT {
        int id PK
        varchar recepitUUID
        varchar receiptName
        varchar address
        date ReceiptDate
        int payerUserId FK
        double totalPrice
        double discountPrice
        boolean discountApplied
        double actualPaidPrice
        int allocationType
        timestamp createdAt
    }
    RECEIPTITEM {
        int id PK
        int receiptId FK
        varchar receiptItemName
        double itemQuantity
        double itemPrice
        int engagerCount
    }
    RECEIPTITEMUSER {
        int id PK
        int receiptItemId FK
        varchar receiptItemUserName
        double itemQuantity
        int userId FK
    }
    REQUIRESTRANSACTION {
        int id PK
        varchar transactionUUID
        int receiptId FK
        int groupId FK
        int senderUser FK
        int recipientUser FK
        double transactionAmount
    }
    OPTIMIZEDTRANSACTION {
        int id PK
        int groupId FK
        int senderUser FK
        int recipientUser FK
        double transactionAmount
        boolean isCleared
        timestamp createdAt
    }
    OPTIMIZEDTRANSACTIONDETAILS {
        int id PK
        int optimizedTransactionId FK
        int requiresTransactionId FK
    }
    GROUPOPTIMIZEDTRANSACTION {
        int id PK
        int groupId FK
        double optimizedAmount
        boolean isCleared
        timestamp createdAt
    }
    GROUPOPTIMIZEDTRANSACTIONDETAILS {
        int id PK
        int groupOptimizedTransactionId FK
        int optimizedTransactionId FK
    }

    USER ||--o{ GROUPUSER : ""
    GROUP ||--o{ GROUPUSER : ""
    USER ||--o{ RECEIPT : ""
    RECEIPT ||--o{ RECEIPTITEM : ""
    RECEIPTITEM ||--o{ RECEIPTITEMUSER : ""
    USER ||--o{ RECEIPTITEMUSER : ""
    RECEIPT ||--o{ REQUIRESTRANSACTION : ""
    GROUP ||--o{ REQUIRESTRANSACTION : ""
    USER ||--o{ REQUIRESTRANSACTION : ""
    GROUP ||--o{ OPTIMIZEDTRANSACTION : ""
    USER ||--o{ OPTIMIZEDTRANSACTION : ""
    OPTIMIZEDTRANSACTION ||--o{ OPTIMIZEDTRANSACTIONDETAILS : ""
    REQUIRESTRANSACTION ||--o{ OPTIMIZEDTRANSACTIONDETAILS : ""
    GROUP ||--o{ GROUPOPTIMIZEDTRANSACTION : ""
    GROUPOPTIMIZEDTRANSACTION ||--o{ GROUPOPTIMIZEDTRANSACTIONDETAILS : ""
    OPTIMIZEDTRANSACTION ||--o{ GROUPOPTIMIZEDTRANSACTIONDETAILS : ""
