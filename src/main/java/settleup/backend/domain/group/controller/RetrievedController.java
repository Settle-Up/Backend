package settleup.backend.domain.group.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.group.entity.dto.GroupInfoListDto;
import settleup.backend.domain.group.service.RetrievedService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.common.ResponseDto;
import settleup.backend.global.exception.CustomException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/group")
public class RetrievedController {
    private final LoginService loginService;
    private final RetrievedService retrievedService;


    @GetMapping("/list/summary")
    public ResponseEntity<ResponseDto> retrievedGroupList(@RequestHeader(value = "Authorization") String token,
                                                          @RequestParam(value = "page", defaultValue = "1") int page,
                                                          @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        UserInfoDto userInfo = loginService.validTokenOrNot(token);
        GroupInfoListDto groupInfoListDtoList = retrievedService.getGroupInfoByUser(userInfo, pageable);
        ResponseDto responseDto = new ResponseDto<>(true, "groupList retrieved successfully ", groupInfoListDtoList);
        return ResponseEntity.ok(responseDto);
    }
    @GetMapping("/user/list")
    public ResponseEntity<ResponseDto> retrievedGroupUserList(
            @RequestHeader(value = "Authorization") String token, @RequestParam("groupId") String groupUUID) throws CustomException {
        loginService.validTokenOrNot(token);
        Map<String, Object> data = new HashMap<>();
        List<UserInfoDto> memberList = retrievedService.getGroupUserInfo(groupUUID);
        data.put("memberList", memberList);
        ResponseDto responseDto = new ResponseDto<>(true,"retrieved successfully",data);
        return ResponseEntity.ok(responseDto);
    }
}
