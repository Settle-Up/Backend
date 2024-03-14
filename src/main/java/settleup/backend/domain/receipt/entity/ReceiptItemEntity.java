package settleup.backend.domain.receipt.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    private Double itemQuantity;

    @Column(name = "item_price", nullable = false)
    private Double itemPrice;

    @Column(name = "engager_count", nullable = false)
    private Integer engagerCount;
}

