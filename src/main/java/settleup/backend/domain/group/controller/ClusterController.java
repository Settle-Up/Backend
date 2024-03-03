package settleup.backend.domain.group.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.group.service.ClusterService;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.ResponseDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;
import settleup.backend.global.exception.ErrorHttpStatusMapping;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/home")
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
}



