package settleup.backend.domain.user.entity;

import settleup.backend.global.Helper.Status;
import jakarta.persistence.*;

@Entity
@Table(name = "settle_user")
public class UserEntity extends AbstractUserEntity {
    @Override
    public Status getUserType() {
        return Status.REGULAR;
    }
}
