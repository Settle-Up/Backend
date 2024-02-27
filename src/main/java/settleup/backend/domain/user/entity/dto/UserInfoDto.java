package settleup.backend.domain.user.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String userUUID;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userName;
    private String userEmail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userPhone;
}



