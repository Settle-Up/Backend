package settleup.backend.domain.group.entity;

import settleup.backend.global.Helper.Status;

import java.time.LocalDateTime;

public interface GroupTypeEntity {
    Long getId();
    String getGroupName();
    String getGroupUUID();
    String getGroupUrl();
    LocalDateTime getCreationTime();

    void setGroupName(String groupName);
    void setGroupUUID(String groupUUID);
    void setGroupUrl(String groupUrl);
    void setCreationTime(LocalDateTime creationTime);
    Status getGroupType();
}
