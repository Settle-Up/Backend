package settleup.backend.domain.group.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.Helper.Status;

@Entity
@Getter
@Setter
@Table(name = "settle_group_user")
public class GroupUserEntity extends AbstractGroupUserEntity {
    @Override
    public Status getGroupUserType() {
        return Status.REGULAR;
    }

    @Override
    public void setUser(AbstractUserEntity user) {
        if (user instanceof UserEntity) {
            super.setUser(user);
        } else {
            throw new IllegalArgumentException("User must be an instance of UserEntity");
        }
    }

    @Override
    public void setGroup(AbstractGroupEntity group) {
        if (group instanceof GroupEntity) {
            super.setGroup(group);
        } else {
            throw new IllegalArgumentException("Group must be an instance of GroupEntity");
        }
    }
}
