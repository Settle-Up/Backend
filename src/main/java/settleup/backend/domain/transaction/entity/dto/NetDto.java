package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.UserTypeEntity;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetDto {
    private UserTypeEntity user;
    private GroupTypeEntity group;
    private BigDecimal netAmount;
}
