package settleup.backend.domain.group.service;

import org.springframework.data.domain.Pageable;
import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.group.entity.dto.GroupMonthlyReportDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;

import java.util.List;
import java.util.Map;

public interface ClusterService {
    CreateGroupResponseDto createGroup(CreateGroupRequestDto requestDto) throws CustomException;

    List<UserInfoDto> getGroupUserInfo(String groupUUID) throws  CustomException;

    GroupMonthlyReportDto givenMonthlyReport(UserInfoDto userInfoDto, String groupId, GroupMonthlyReportDto groupMonthlyReportDto) throws CustomException;

    Map<String, String> deleteGroupUserInfo(UserInfoDto userInfoDto, String groupId) throws CustomException;

    CreateGroupResponseDto inviteGroupFundamental(CreateGroupRequestDto requestDto,String groupId)throws CustomException;
}
