package settleup.backend.domain.receipt.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "receipt_item")
@Getter
@Setter
public class ReceiptItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_id", nullable = false)
    private ReceiptEntity receipt;

    @Column(name = "receipt_item_name", nullable = false)
    private String receiptItemName;

    @Column(name = "item_quantity", nullable = false)
    private BigDecimal itemQuantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "joint_purchaser_count", nullable = false)
    private Integer jointPurchaserCount;
}

