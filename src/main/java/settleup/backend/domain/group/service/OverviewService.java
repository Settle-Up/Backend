package settleup.backend.domain.group.service;

import settleup.backend.domain.group.entity.dto.GroupOverviewDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;

import java.util.Map;

public interface OverviewService {
    GroupOverviewDto retrievedOverview(String groupUUID, UserInfoDto userInfoDto) throws CustomException;
}
