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
    private List<UserGroupListDto> groupList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGroupListDto {
        private String groupId;
        private String groupName;
        private String groupMemberCount;
        private String net;
        private String lastActive;
    }
}
