package settleup.backend.domain.group.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.user.entity.DemoUserEntity;
import settleup.backend.domain.user.entity.UserTypeEntity;
import settleup.backend.global.Helper.Status;

import java.io.Serializable;

@Entity
@Setter
@Getter
@Table(name = "settle_demo_group_user")
public class DemoGroupUserEntity implements GroupUserTypeEntity,Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private DemoUserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private DemoGroupEntity group;

    @Column(name = "is_monthly_report_update_on", nullable = false)
    private Boolean isMonthlyReportUpdateOn;

    @Override
    public void setIsMonthlyReportUpdateOn(Boolean isMonthlyReportUpdateOn) {
        this.isMonthlyReportUpdateOn = isMonthlyReportUpdateOn;
    }

    @Override
    public UserTypeEntity getUser() {
        return user;
    }

    @Override
    public GroupTypeEntity getGroup() {
        return group;
    }

    @Override
    public void setUser(UserTypeEntity user) {
        this.user = (DemoUserEntity) user;
    }

    @Override
    public void setGroup(GroupTypeEntity group) {
        this.group = (DemoGroupEntity) group;
    }

    @Override
    public Status getGroupUserType() {
        return Status.DEMO;
    }
}
