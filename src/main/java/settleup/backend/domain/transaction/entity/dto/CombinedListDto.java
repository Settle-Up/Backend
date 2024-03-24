package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import settleup.backend.domain.user.entity.UserEntity;

@Data
@AllArgsConstructor
public class CombinedListDto {
    private String optimizedUUID;
    private UserEntity senderUser;
    private UserEntity recipientUser;
    private double optimizedAmount;

}
