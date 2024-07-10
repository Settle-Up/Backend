

package settleup.backend.domain.user.entity;

import settleup.backend.global.Helper.Status;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "settle_demo_user")
public class DemoUserEntity extends AbstractUserEntity {
    @Column(nullable = false)
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public Status getUserType() {
        return Status.DEMO;
    }
}
