package settleup.backend.domain.group.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMonthlyReportDto {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String groupId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String groupName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userName;
    private Boolean isMonthlyReportUpdateOn;
}