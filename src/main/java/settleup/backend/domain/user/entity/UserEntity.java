package settleup.backend.domain.user.entity;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_uuid",nullable = false,unique = true)
    private String userUUID;
    @Column(nullable = false)
    private String userName;
    @Column(nullable = false)
    private String userPhone;
    @Column(nullable = false)
    private String userEmail;
}
