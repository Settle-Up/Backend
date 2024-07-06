package settleup.backend.domain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.global.Helper.Status;

import java.util.Date;

/**
 * 데모 유저에도 핸드폰 번호 , 이메일이 있는이유
 * 데모유저 더미데이터 생성때문에 ...
 */
@Entity
@Getter
@Setter
@Table(name = "settle_demo_user")
public class DemoUserEntity implements UserTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_uuid",nullable = false,unique = true)
    private String userUUID;
    @Column(nullable = false)
    private String userName;
    @Column(nullable = true)
    private String userPhone;
    @Column(nullable = false,unique = true)
    private String userEmail;
    @Column(nullable = true)
    private Boolean isDecimalInputOption;
    @Column(nullable = false)
    private Date createdAt;

    @Override
    public Status getUserType() {
        return Status.DEMO;
    }
}

