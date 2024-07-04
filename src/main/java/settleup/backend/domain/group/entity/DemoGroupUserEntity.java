package settleup.backend.domain.group.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.domain.user.entity.DemoUserEntity;
import settleup.backend.domain.user.entity.UserEntity;

import java.io.Serializable;

@Entity
@Setter
@Getter
@Table(name = "settle_demo_group_user")
public class DemoGroupUserEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demo_user_id", nullable = false)
    private DemoUserEntity demoUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "demo_group_id", nullable = false)
    private DemoGroupEntity demoGroup;

    @Column(name = "is_monthly_report_update_on", nullable = false)
    private Boolean isMonthlyReportUpdateOn;

    public void setIsMonthlyReportUpdateOn(boolean isMonthlyReportUpdateOn) {
        this.isMonthlyReportUpdateOn = isMonthlyReportUpdateOn;
    }

}
