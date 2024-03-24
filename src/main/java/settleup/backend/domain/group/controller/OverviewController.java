package settleup.backend.domain.group.controller;


import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
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

    @GetMapping("/list/detail")
    public ResponseEntity<ResponseDto> retrievedDetailPage
            (@RequestHeader(value = "Authorization") String token, @RequestParam("groupId") String groupUUID) {
        UserInfoDto userInfoDto=loginService.validTokenOrNot(token);
        overviewService.retrievedOverview(groupUUID,userInfoDto);

        return null;

    }
}
