package settleup.backend.domain.transaction.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.user.entity.UserEntity;

import java.time.LocalDateTime;
@Entity
@Setter
@Getter
@Table(name = "GroupOptimizedTransaction")
public class GroupOptimizedTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String groupOptimizedTransactionUUID;

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
    private boolean isCleared;
    @Column(nullable = false)
    private LocalDateTime createdAt;

}