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
public class OptimizedTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String optimizedTransactionUUID; // OPT 로 시작

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_Id")
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private UserEntity senderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private UserEntity recipientUser;

    @Column(nullable = false)
    private double transactionAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status isCleared;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)

    private Status isUsed;
    @Column(nullable = false)
    private LocalDateTime createdAt;
}