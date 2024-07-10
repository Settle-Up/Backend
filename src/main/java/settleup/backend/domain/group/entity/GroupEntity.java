package settleup.backend.domain.group.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.global.Helper.Status;

@Entity
@Getter
@Setter
@Table(name = "settle_group")
public class GroupEntity extends AbstractGroupEntity {
    @Override
    public Status getGroupType() {
        return Status.REGULAR;
    }
}
