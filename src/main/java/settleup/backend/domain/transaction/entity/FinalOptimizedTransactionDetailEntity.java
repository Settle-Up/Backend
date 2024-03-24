package settleup.backend.domain.transaction.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "final_optimized_transaction_details")
public class FinalOptimizedTransactionDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String finalOptimizedTransactionDetailUUID;


    @Column(name = "used_optimized_transaction_uuid")
    private String usedOptimizedTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "final_Transaction_id")
    private FinalOptimizedTransactionEntity finalOptimizedTransaction;

}
