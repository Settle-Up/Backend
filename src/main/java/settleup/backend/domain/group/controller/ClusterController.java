package settleup.backend.domain.group.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.group.entity.dto.GroupMonthlyReportDto;
import settleup.backend.domain.group.service.ClusterService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.common.ResponseDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("")
public class ClusterController {

    private LoginService loginService;
    private ClusterService clusterService;

    @PostMapping("/group/create")
    public ResponseEntity<ResponseDto> makeGroup(
            @RequestHeader(value = "Authorization") String token, @RequestBody CreateGroupRequestDto requestDto) throws CustomException {
        loginService.validTokenOrNot(token);

        int groupMemberCount = Integer.parseInt(requestDto.getGroupMemberCount());
        if (requestDto.getGroupUserList().size() != groupMemberCount) {
            throw new CustomException(ErrorCode.INVALID_INPUT);
        }

        CreateGroupResponseDto responseDto = clusterService.createGroup(requestDto);
        ResponseDto<CreateGroupResponseDto> responseDtoForClient = new ResponseDto<>(true, "Group create successfully", responseDto);
        return ResponseEntity.ok(responseDtoForClient);
    }

    @GetMapping("/group/user/list")
    public ResponseEntity<ResponseDto> retrievedGroupUserList(
            @RequestHeader(value = "Authorization") String token, @RequestParam("groupId") String groupUUID) throws CustomException {
        loginService.validTokenOrNot(token);
        Map<String, Object> data = new HashMap<>();
        List<UserInfoDto> memberList = clusterService.getGroupUserInfo(groupUUID);
        data.put("memberList", memberList);
        ResponseDto responseDto = new ResponseDto<>(true,"retrieved successfully",data);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/group/alarm")
    public ResponseEntity<ResponseDto> groupMonthlyReport(
            @RequestHeader(value = "Authorization") String token, @RequestParam("groupId") String groupUUID, @RequestBody GroupMonthlyReportDto groupMonthlyReportDto) throws CustomException {
        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        GroupMonthlyReportDto data = clusterService.givenMonthlyReport(userInfoDto, groupUUID, groupMonthlyReportDto);
        ResponseDto responseDto = new ResponseDto<>(true, "user alarm status update successfully", data);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/group/remove")
    public ResponseEntity<ResponseDto> withdrawalGroup(
            @RequestHeader(value = "Authorization") String token, @RequestParam("groupId") String groupUUID) {
        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        Map<String, String> data = clusterService.deleteGroupUserInfo(userInfoDto, groupUUID);
        ResponseDto responseDto = new ResponseDto<>(true, "user Group Exit Completed", data);
        return ResponseEntity.ok(responseDto);
    }
}



