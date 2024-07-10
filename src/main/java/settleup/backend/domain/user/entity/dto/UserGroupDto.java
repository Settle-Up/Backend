package settleup.backend.domain.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.group.entity.AbstractGroupUserEntity;
import settleup.backend.domain.user.entity.AbstractUserEntity;


import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupDto {
    AbstractGroupEntity group;
    List<AbstractUserEntity> userEntityList;
    AbstractGroupUserEntity groupUser;
    AbstractUserEntity singleUser;
    Boolean isUserType;
}
