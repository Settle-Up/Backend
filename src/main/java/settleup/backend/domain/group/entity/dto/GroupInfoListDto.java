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
    private List<UserGroupListDto> groupList; // 클래스 이름은 대문자로 시작해야 합니다.

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserGroupListDto {
        private String groupId;
        private String groupName;
        private Integer groupMemberCount;
        private Float net;
        private String lastActive;
    }
}
