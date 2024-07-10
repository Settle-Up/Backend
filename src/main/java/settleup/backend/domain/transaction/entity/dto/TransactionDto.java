package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.user.entity.AbstractUserEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private ReceiptEntity receipt;
    private Boolean isUserType;
    private AbstractGroupEntity group;
    private String allocationType;
    private AbstractUserEntity payerUser;
    private List<AbstractUserEntity> owedUser;
}
