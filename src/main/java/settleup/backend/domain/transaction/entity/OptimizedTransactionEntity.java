package settleup.backend.domain.transaction.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.common.Status;

import java.time.LocalDateTime;


@Entity
@Setter
@Getter
@Table(name = "OptimizedTransaction")
public class OptimizedTransactionEntity implements TransactionalEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_uuid", nullable = false, unique = true)
    private String transactionUUID; // OPT 로 시작

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_Id",nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_user",nullable = false)
    private UserEntity senderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_user",nullable = false)
    private UserEntity recipientUser;

    @Column(name = "transaction_amount", nullable = false)
    private double transactionAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status isUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_sender_status", nullable = false)
    private Status isSenderStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_recipient_status", nullable = false)
    private Status isRecipientStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_inheritances_status", nullable = false)
    private Status isInheritanceStatus;

    @Column(name = "clear_status_timestamp")
    @Setter
    private LocalDateTime clearStatusTimestamp;



    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Override
    public Long getId(){return this.id;}
    @Override
    public String getTransactionUUID() {
        return this.transactionUUID;
    }

    @Override
    public UserEntity getSenderUser(){return this.senderUser;}

    @Override
    public GroupEntity getGroup() {
        return this.group;
    }

    @Override
    public UserEntity getRecipientUser(){return  this.recipientUser;}


    @Override
    public Status getIsSenderStatus() {
        return this.isSenderStatus;
    }

    @Override
    public Status getIsRecipientStatus() {
        return this.isRecipientStatus;
    }

    @Override
    public LocalDateTime getClearStatusTimeStamp() {
        return this.clearStatusTimestamp;
    }

    @Override
    public double getTransactionAmount(){return  this.transactionAmount;}

}
