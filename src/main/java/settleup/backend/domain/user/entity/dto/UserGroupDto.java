package settleup.backend.domain.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.user.entity.UserEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupDto {
    GroupEntity group;
    List<UserEntity> userEntityList;
}
