package settleup.backend.domain.group.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupInfoListDto {
    private String userId;
    private String userName;
    private List<userGroupListDto> groupList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class userGroupListDto {
        private String groupId;
        private String groupName;
        private float net;
        private String lastActive;
    }
}
