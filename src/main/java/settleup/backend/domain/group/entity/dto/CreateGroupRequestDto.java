package settleup.backend.domain.group.entity.dto;

import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateGroupRequestDto {
    private String groupName;
    private String groupMemberCount;
    private List<String> groupUserList;
}
