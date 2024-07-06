package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.UserTypeEntity;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {
    private ReceiptEntity receipt;
    private Boolean isUserType;
    private GroupTypeEntity group;
    private String allocationType;
    private UserTypeEntity payerUser;
    private List<UserTypeEntity> owedUser;
}
