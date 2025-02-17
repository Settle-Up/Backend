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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isDecimalInputOption;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean isRegularUserOrDemoUser; // 일반 유저 true , 데모유저 false // 유의 할점 클라이언트에게 안가도록 해야함 서버내에서만 써야함
}



