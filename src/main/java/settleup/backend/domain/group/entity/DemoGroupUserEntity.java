package settleup.backend.domain.group.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.domain.user.entity.DemoUserEntity;
import settleup.backend.global.Helper.Status;

@Entity
@Getter
@Setter
@Table(name = "settle_demo_group_user")
public class DemoGroupUserEntity extends AbstractGroupUserEntity {
    @Override
    public Status getGroupUserType() {
        return Status.DEMO;
    }

    @Override
    public void setUser(AbstractUserEntity user) {
        if (user instanceof DemoUserEntity) {
            super.setUser(user);
        } else {
            throw new IllegalArgumentException("User must be an instance of DemoUserEntity");
        }
    }

    @Override
    public void setGroup(AbstractGroupEntity group) {
        if (group instanceof DemoGroupEntity) {
            super.setGroup(group);
        } else {
            throw new IllegalArgumentException("Group must be an instance of DemoGroupEntity");
        }
    }
}
