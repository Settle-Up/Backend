package settleup.backend.domain.transaction.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;

@Entity
@Setter
@Getter
@Table(name = "ultimate_optimized_transaction_details")
public class UltimateOptimizedTransactionDetailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_detail_uuid",nullable = false,unique = true)
    private String transactionDetailUUID;


    @Column(name = "used_optimized_transaction_uuid")
    private String usedOptimizedTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ultimate_transaction_id")
    private UltimateOptimizedTransactionEntity ultimateOptimizedTransaction;

}
