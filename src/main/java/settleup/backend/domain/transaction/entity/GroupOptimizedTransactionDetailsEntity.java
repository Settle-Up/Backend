package settleup.backend.domain.transaction.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "GroupOptimizedTransactionDetails")
public class GroupOptimizedTransactionDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_detail_uuid",nullable = false,unique = true)
    private String transactionDetailUUID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groupOptimizedTransaction_id")
    private GroupOptimizedTransactionEntity groupOptimizedTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optimizedTransaction_id")
    private OptimizedTransactionEntity optimizedTransaction;

}

