package settleup.backend.domain.group.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMonthlyReportDto {
    private String groupId;
    private String groupName;
    private String userId;
    private String userName;
    private Boolean isMonthlyReportUpdateOn;
}
