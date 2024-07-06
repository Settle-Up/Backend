package settleup.backend.domain.group.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import settleup.backend.global.Helper.Status;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "settle_demo_group")
public class DemoGroupEntity implements GroupTypeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_name", nullable = false)
    private String groupName;

    @Column(name = "group_uuid", nullable = false, unique = true)
    private String groupUUID;

    @Column(name = "group_url", nullable = false, unique = true)
    private String groupUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime creationTime;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public String getGroupName() {
        return groupName;
    }

    @Override
    public String getGroupUUID() {
        return groupUUID;
    }

    @Override
    public String getGroupUrl() {
        return groupUrl;
    }

    @Override
    public LocalDateTime getCreationTime() {
        return creationTime;
    }

    @Override
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public void setGroupUUID(String groupUUID) {
        this.groupUUID = groupUUID;
    }

    @Override
    public void setGroupUrl(String groupUrl) {
        this.groupUrl = groupUrl;
    }

    @Override
    public void setCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public Status getGroupType() {
        return Status.DEMO;
    }
}
