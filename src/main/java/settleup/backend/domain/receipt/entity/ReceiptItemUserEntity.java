package settleup.backend.domain.receipt.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.user.entity.UserEntity;
@Entity
@Table(name = "receipt_item_user")
@Getter
@Setter
public class ReceiptItemUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receipt_item_id", nullable = false)
    private ReceiptItemEntity receiptItem;


    @Column(name = "purchased_quantity",nullable = true)
    private Double purchasedQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
}

