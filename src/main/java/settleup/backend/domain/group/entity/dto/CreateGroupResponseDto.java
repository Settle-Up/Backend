package settleup.backend.domain.group.entity.dto;

import lombok.*;
import settleup.backend.domain.user.entity.dto.UserInfoDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
/**
 * BE 로직상 groupId filed 는 groupUUID , FE 에게 보내주기 위한 컨벤션
 */
public class CreateGroupResponseDto {
    private String groupId;
    private String groupName;
    private String groupMemberCount;
    private String groupUrl;
    private List<UserInfoDto> userList;
}
