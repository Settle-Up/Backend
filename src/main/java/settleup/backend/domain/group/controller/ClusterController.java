package settleup.backend.domain.group.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.group.entity.dto.GroupMonthlyReportDto;
import settleup.backend.domain.group.service.ClusterService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.Helper.ResponseDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/groups")
public class ClusterController {

    private LoginService loginService;
    private ClusterService clusterService;

    @PostMapping("")
    public ResponseEntity<ResponseDto> makeGroup(
            @RequestHeader(value = "Authorization") String token, @RequestBody CreateGroupRequestDto requestDto) throws CustomException {
        UserInfoDto userInfo = loginService.validTokenOrNot(token);
        Boolean isRegularUser = userInfo.getIsRegularUserOrDemoUser();

        int groupMemberCount = Integer.parseInt(requestDto.getGroupMemberCount());
        if (requestDto.getGroupUserList().size() != groupMemberCount) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        CreateGroupResponseDto responseDto = clusterService.createGroup(requestDto, isRegularUser);
        ResponseDto<CreateGroupResponseDto> responseDtoForClient =
                new ResponseDto<>(true, "Group create successfully", responseDto);
        return ResponseEntity.ok(responseDtoForClient);
    }


    @PatchMapping("/{groupId}/monthly-reports")
    public ResponseEntity<ResponseDto> groupMonthlyReport(
            @RequestHeader(value = "Authorization") String token,
            @PathVariable("groupId") String groupUUID,
            @RequestBody GroupMonthlyReportDto groupMonthlyReportDto) throws CustomException {

        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        Boolean isRegularUser = userInfoDto.getIsRegularUserOrDemoUser();
        GroupMonthlyReportDto data = clusterService.givenMonthlyReport(userInfoDto, groupUUID, groupMonthlyReportDto,isRegularUser);

        ResponseDto responseDto = new ResponseDto<>(true, "user alarm status update successfully", data);
        return ResponseEntity.ok(responseDto);

    }

    @DeleteMapping("/{groupID}")
    public ResponseEntity<ResponseDto> withdrawalGroup(
            @RequestHeader(value = "Authorization") String token,
            @PathVariable("groupID") String groupUUID) {
        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        Boolean isRegularUser = userInfoDto.getIsRegularUserOrDemoUser();
        Map<String, String> data = clusterService.deleteGroupUserInfo(userInfoDto, groupUUID,isRegularUser);
        ResponseDto responseDto = new ResponseDto<>(true, "user Group Exit Completed", data);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/{groupId}/members")
    public ResponseEntity<ResponseDto> inviteFromOurSite(
            @RequestHeader(value = "Authorization") String token,
            @RequestBody CreateGroupRequestDto requestDto,
            @PathVariable("groupId") String groupId) throws CustomException {

        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        Boolean isRegularUser = userInfoDto.getIsRegularUserOrDemoUser();

        int groupMemberCount = Integer.parseInt(requestDto.getGroupMemberCount());
        if (requestDto.getGroupUserList().size() != groupMemberCount) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        CreateGroupResponseDto responseDto = clusterService.inviteGroupFundamental(requestDto, groupId,isRegularUser);
        ResponseDto<CreateGroupResponseDto> responseDtoForClient =
                new ResponseDto<>(true, "User has been invited successfully", responseDto);
        return ResponseEntity.ok(responseDtoForClient);
    }
}



