package settleup.backend.domain.user.entity;

import settleup.backend.global.Helper.Status;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "abstract_user")
public abstract class AbstractUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_uuid", nullable = false, unique = true)
    private String userUUID;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = true)
    private String userPhone;

    @Column(nullable = false, unique = true)
    private String userEmail;

    @Column(nullable = true)
    private Boolean isDecimalInputOption;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private Status userType;

    // 공통 필드와 메서드에 대한 Getter와 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserUUID() {
        return userUUID;
    }

    public void setUserUUID(String userUUID) {
        this.userUUID = userUUID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Boolean getIsDecimalInputOption() {
        return isDecimalInputOption;
    }

    public void setIsDecimalInputOption(Boolean isDecimalInputOption) {
        this.isDecimalInputOption = isDecimalInputOption;
    }

    public Status getUserType() {
        return userType;
    }

    public void setUserType(Status userType) {
        this.userType = userType;
    }
}
