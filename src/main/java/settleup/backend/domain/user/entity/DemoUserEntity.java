
package settleup.backend.domain.user.entity;

import settleup.backend.global.Helper.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "settle_demo_user")
public class DemoUserEntity extends AbstractUserEntity {
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private String ip;

    @Column(nullable = false)
    private boolean isDummy; // New field to identify dummy users

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public boolean isDummy() {
        return isDummy;
    }

    public void setDummy(boolean isDummy) {
        this.isDummy = isDummy;
    }

    @Override
    public Status getUserType() {
        return Status.DEMO;
    }
}
