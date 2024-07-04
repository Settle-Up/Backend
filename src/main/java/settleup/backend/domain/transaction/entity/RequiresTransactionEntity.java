package settleup.backend.domain.transaction.entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.common.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "requires_transaction")
@Getter
@Setter
public class RequiresTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_uuid", nullable = false, unique = true)
    private String transactionUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private ReceiptEntity receipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user", nullable = false)
    private UserEntity senderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user", nullable = false)
    private UserEntity recipientUser;

    @Column(name = "transaction_amount", nullable = false)
    private BigDecimal transactionAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "require_reflection", nullable = false)
    private Status requiredReflection;

    @Column(name = "clear_status_timestamp")
    @Setter
    @Getter
    private LocalDateTime clearStatusTimestamp;

}

