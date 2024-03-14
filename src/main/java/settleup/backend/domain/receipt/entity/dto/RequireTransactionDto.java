package settleup.backend.domain.receipt.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.user.entity.UserEntity;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequireTransactionDto {
    private ReceiptEntity receipt;
    private GroupEntity group;
    private String allocationType;
    private UserEntity payerUser;

}
