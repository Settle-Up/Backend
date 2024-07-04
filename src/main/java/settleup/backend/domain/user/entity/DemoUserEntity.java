package settleup.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "settle_demo_user")
public class DemoUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "demo_user_uuid",nullable = false,unique = true)
    private String DemoUserUUID;
    @Column(nullable = false)
    private String userName;
    @Column(nullable = true)
    private String userPhone;
    @Column(nullable = true)
    private String userEmail;
    @Column(nullable = true)
    private Boolean isDecimalInputOption;
}

