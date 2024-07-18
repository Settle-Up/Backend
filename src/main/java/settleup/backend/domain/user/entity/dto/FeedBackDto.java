package settleup.backend.domain.user.entity.dto;

import lombok.Data;

@Data
public class FeedBackDto {
    private Boolean serverOrClient;
    private String issueLocation;
    private String issueDescription;
    private String replyEmailAddress;

}
