package settleup.backend.domain.group.controller;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.group.service.ClusterService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;
import settleup.backend.global.exception.ErrorHttpStatusMapping;

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
}



