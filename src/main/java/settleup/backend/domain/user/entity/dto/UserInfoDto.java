package settleup.backend.domain.user.entity.dto;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String userUUID;
    private String userName;
    private String userEmail;
    private String userPhone;

}




