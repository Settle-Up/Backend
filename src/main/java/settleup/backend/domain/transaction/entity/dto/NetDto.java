package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.user.entity.UserEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetDto {
    private UserEntity user;
    private GroupEntity group;
    private float netAmount;
}
