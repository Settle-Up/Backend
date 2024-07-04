package settleup.backend.domain.group.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@Table(name = "settle_demo_group")
public class DemoGroupEntity {
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
}
