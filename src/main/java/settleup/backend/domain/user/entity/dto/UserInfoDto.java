package settleup.backend.domain.user.entity.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    //TODO 3.4 userUUID userId 로 변경해서 줘야함
    //TODO 3.4 현재 dto 는 전역적으로 쓰이는데, dto를 변경할지 응답에서 변경할지 고민중
    private String userUUID;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userName;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userEmail;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userPhone;
}



