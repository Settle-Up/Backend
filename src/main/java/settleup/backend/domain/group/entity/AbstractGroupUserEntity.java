package settleup.backend.domain.group.entity;

import settleup.backend.domain.user.entity.AbstractUserEntity;
import settleup.backend.global.Helper.Status;
import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "abstract_group_user")
public abstract class AbstractGroupUserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_monthly_report_update_on", nullable = false)
    private Boolean isMonthlyReportUpdateOn;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AbstractUserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private AbstractGroupEntity group;

    // 공통 필드와 메서드에 대한 Getter와 Setter
    public abstract Status getGroupUserType();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIsMonthlyReportUpdateOn() {
        return isMonthlyReportUpdateOn;
    }

    public void setIsMonthlyReportUpdateOn(Boolean isMonthlyReportUpdateOn) {
        this.isMonthlyReportUpdateOn = isMonthlyReportUpdateOn;
    }

    public AbstractUserEntity getUser() {
        return user;
    }

    public void setUser(AbstractUserEntity user) {
        this.user = user;
    }

    public AbstractGroupEntity getGroup() {
        return group;
    }

    public void setGroup(AbstractGroupEntity group) {
        this.group = group;
    }
}
