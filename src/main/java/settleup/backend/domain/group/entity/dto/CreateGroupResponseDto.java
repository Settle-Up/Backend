package settleup.backend.domain.group.entity.dto;

import lombok.*;
import settleup.backend.domain.user.entity.dto.UserInfoDto;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class CreateGroupResponseDto {
    private String groupUUID;
    private String groupName;
    private String groupMemberCount;
    private String groupUrl;
    private List<UserInfoDto> userList;
}
