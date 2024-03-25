package settleup.backend.domain.group.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.group.entity.dto.GroupOverviewDto;
import settleup.backend.domain.group.service.OverviewService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.api.ResponseDto;

@RestController
@AllArgsConstructor
@RequestMapping("group/")
public class OverviewController {
    private final LoginService loginService;
    private final OverviewService overviewService;

//    @GetMapping("/list/detail")
//    public ResponseEntity<ResponseDto> retrievedDetailPage
//            (@RequestHeader(value = "Authorization") String token, @RequestParam("groupId") String groupUUID) {
//        UserInfoDto userInfoDto=loginService.validTokenOrNot(token);
//        GroupOverviewDto data=overviewService.retrievedOverview(groupUUID,userInfoDto);
//        ResponseDto responseDto = new ResponseDto<>(true,"",data);
//        return ResponseEntity.ok(responseDto);
//
//    }
    @GetMapping("/list/detail")
    public ResponseEntity<ResponseDto> retrievedDetailPage
            ( @RequestParam("groupId") String groupUUID,@RequestParam("userId") String userId ){
        UserInfoDto userInfoDto =new UserInfoDto();
        userInfoDto.setUserId(userId);
        GroupOverviewDto data=overviewService.retrievedOverview(groupUUID,userInfoDto);
        ResponseDto responseDto = new ResponseDto<>(true,"",data);
        return ResponseEntity.ok(responseDto);

    }
}
