package settleup.backend.domain.transaction.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


@Entity
@Setter
@Getter
@Table(name = "OptimizedTransactionDetails")
public class OptimizedTransactionDetailsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String optimizedTransactionDetailUUID;//OPTD 로 시작

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "optimizedTransaction_id")
    private OptimizedTransactionEntity optimizedTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requiresTransaction_id")
    private RequiresTransactionEntity requiresTransaction;


}