package settleup.backend.domain.user.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
import settleup.backend.domain.group.entity.GroupUserEntity;
import settleup.backend.domain.group.entity.GroupUserTypeEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.UserTypeEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserGroupDto {
    GroupTypeEntity group;
    List<UserTypeEntity> userEntityList;
    GroupUserTypeEntity groupUser;
    UserTypeEntity singleUser;
    Boolean isUserType;
}
