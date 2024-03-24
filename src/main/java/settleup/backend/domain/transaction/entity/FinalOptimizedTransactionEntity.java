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
@Table(name = "final_optimized_transaction")
public class FinalOptimizedTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String finalOptimizedTransactionUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupId")
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senderUser")
    private UserEntity senderUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipientUser")
    private UserEntity recipientUser;

    @Column(nullable = false)
    private double optimizedAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status isCleared;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status isUsed;

    @Column(nullable = false)
    private LocalDateTime createdAt;

}
