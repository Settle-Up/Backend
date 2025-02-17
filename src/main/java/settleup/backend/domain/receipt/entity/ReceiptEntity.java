package settleup.backend.domain.receipt.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.global.Helper.Status;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "receipt")
@Getter
@Setter
public class ReceiptEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "receipt_uuid", nullable = false, unique = true)
    private String receiptUUID;

    @Column(name = "receipt_name", nullable = false)
    private String receiptName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private AbstractGroupEntity group; // 추상 클래스 사용

    @Column(nullable = false)
    private String address;

    @Column(name = "receipt_date", nullable = false)
    private String receiptDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_user_id", nullable = false)
    private AbstractUserEntity payerUser; // 추상 클래스 사용

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "discount_applied", nullable = false)
    private BigDecimal discountApplied;

    @Column(name = "actual_paid_price", nullable = false)
    private BigDecimal actualPaidPrice;

    @Column(name = "allocation_type", nullable = false)
    private String allocationType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private Status userType;
}
