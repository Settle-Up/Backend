package settleup.backend.domain.transaction.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import settleup.backend.domain.receipt.entity.dto.ReceiptDto;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
/**
 *  더 필요한거 없는지 생각 해보기
 *  그리고 userListDto에 userUUID 꼭 필요하지 않으면 지우기
 */
public class OptimizationTargetDto {
    private String userId;
    private String userUUID;
    private String groupId;
    private String groupUUID;
    private List<userListDto> userList;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class userListDto {
        private String userId;
        private String userUUID;
    }

}
