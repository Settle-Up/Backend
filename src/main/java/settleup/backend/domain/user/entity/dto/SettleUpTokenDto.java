package settleup.backend.domain.user.entity.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SettleUpTokenDto {
    private String accessToken;
    private String subject;
    private String issuedTime;
    private String expiresIn;
    private String userName;
    private String userUUID;
}
