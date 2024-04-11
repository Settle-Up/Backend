package settleup.backend.domain.group.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.dto.GroupOverviewDto;
import settleup.backend.domain.group.entity.dto.GroupOverviewExpenseDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;

import java.util.Map;

public interface OverviewService {
    GroupOverviewDto retrievedOverview(String groupUUID, UserInfoDto userInfoDto) throws CustomException;

    GroupOverviewExpenseDto updateRetrievedExpenseList(GroupOverviewExpenseDto overviewDto, String groupUUID, UserInfoDto userInfoDto, Pageable pageable) throws CustomException;



}
