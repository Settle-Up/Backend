package settleup.backend.domain.user.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
/**
 * BE 로직상 userId filed 는 userUUID , FE 에게 보내주기 위한 컨벤션
 */
public class UserInfoDto {
    private String userId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userEmail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userPhone;
}



