package settleup.backend.domain.group.entity;

import settleup.backend.domain.user.entity.UserTypeEntity;
import settleup.backend.global.Helper.Status;

public interface GroupUserTypeEntity {
    Long getId();
    Boolean getIsMonthlyReportUpdateOn();
    void setIsMonthlyReportUpdateOn(Boolean isMonthlyReportUpdateOn);

    UserTypeEntity getUser();
    GroupTypeEntity getGroup();
    void setUser(UserTypeEntity user);
    void setGroup(GroupTypeEntity group);
    Status getGroupUserType();
}