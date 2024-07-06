package settleup.backend.domain.transaction.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.transaction.model.TransactionalEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.Helper.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "ultimate_optimized_transaction")
public class UltimateOptimizedTransactionEntity implements TransactionalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_uuid", nullable = false, unique = true)
    private String transactionUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_Id")
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user")
    private UserEntity senderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user")
    private UserEntity recipientUser;

    @Column(name = "transaction_amount", nullable = false)
    private BigDecimal transactionAmount;

    @Column(name = "optimization_status",nullable = false)
    @Enumerated(EnumType.STRING)
    private Status optimizationStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "has_been_sent_status", nullable = false)
    private Boolean hasBeenSent;


    @Column(name = "has_been_check_status", nullable = false)
    private Boolean hasBeenChecked;

    @Enumerated(EnumType.STRING)
    @Column(name = "require_reflection", nullable = false)
    private Status requiredReflection;


    @Column(name = "clear_status_timestamp")
    @Setter
    @Getter
    private LocalDateTime clearStatusTimestamp;


    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private Status userType;




    @Override
    public Long getId() {
        return this.id;
    }

    @Override
    public UserEntity getSenderUser() {
        return this.senderUser;
    }

    @Override
    public GroupEntity getGroup() {
        return this.group;
    }

    @Override
    public UserEntity getRecipientUser() {
        return this.recipientUser;
    }

    @Override
    public BigDecimal getTransactionAmount() {
        return this.transactionAmount;
    }

    @Override
    public String getTransactionUUID() {
        return this.transactionUUID;
    }

    @Override
    public Boolean getHasBeenSent() {
        return this.hasBeenSent;
    }

    @Override
    public Boolean getHasBeenChecked() {
        return this.hasBeenChecked;
    }

    @Override
    public Status getRequiredReflection() {
        return this.requiredReflection;
    }

    @Override
    public LocalDateTime getCreatedAt(){return this.createdAt;}

    @Override
    public LocalDateTime getClearStatusTimeStamp() {
        return this.clearStatusTimestamp;
    }

    @Override
    public Status getUserType() {
        return this.userType;
    }


}