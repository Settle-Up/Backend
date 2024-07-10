package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.user.entity.AbstractUserEntity;


import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NetDto {
    private AbstractUserEntity user;
    private AbstractGroupEntity group;
    private BigDecimal netAmount;
}
