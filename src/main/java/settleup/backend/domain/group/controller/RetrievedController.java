package settleup.backend.domain.group.controller;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.group.entity.dto.GroupInfoListDto;
import settleup.backend.domain.group.service.RetrievedService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.Helper.ResponseDto;
import settleup.backend.global.exception.CustomException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/groups")
public class RetrievedController {
    private final LoginService loginService;
    private final RetrievedService retrievedService;
    private static final Logger logger = LoggerFactory.getLogger(RetrievedController.class);


    @GetMapping("")
    public ResponseEntity<ResponseDto> retrievedGroupList(@RequestHeader(value = "Authorization") String token,
                                                          @RequestParam(value = "page", defaultValue = "1") int page,
                                                          @RequestParam(value = "size", defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        UserInfoDto userInfo = loginService.validTokenOrNot(token);
        System.out.println("userUUID : "+userInfo.getUserId());
        System.out.println("userDemo : "+ userInfo.getIsRegularUserOrDemoUser());
        GroupInfoListDto groupInfoListDtoList = retrievedService.getGroupInfoByUser(userInfo, pageable);
        ResponseDto responseDto = new ResponseDto<>(true, "groupList retrieved successfully ", groupInfoListDtoList);
        return ResponseEntity.ok(responseDto);
    }


    @GetMapping("/{groupId}/members")
    public ResponseEntity<ResponseDto> retrievedGroupUserList(
            @RequestHeader(value = "Authorization") String token,
            @PathVariable("groupId") String groupUUID) throws CustomException {
        UserInfoDto userInfo = loginService.validTokenOrNot(token);
        Boolean isRegularUser = userInfo.getIsRegularUserOrDemoUser();
        Map<String, Object> data = new HashMap<>();
        List<UserInfoDto> memberList = retrievedService.getGroupUserInfo(groupUUID,isRegularUser);
        data.put("memberList", memberList);
        ResponseDto responseDto = new ResponseDto<>(true,"retrieved successfully",data);
        return ResponseEntity.ok(responseDto);
    }
}
