package settleup.backend.domain.group.controller;


import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.group.entity.dto.GroupOverviewDto;
import settleup.backend.domain.group.service.OverviewService;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.global.common.ResponseDto;

@RestController
@AllArgsConstructor
@RequestMapping("/group")
public class OverviewController {
    private final LoginService loginService;
    private final OverviewService overviewService;


    @GetMapping("/overview")
    public ResponseEntity<ResponseDto> getGroupOverview(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam("groupId") String groupId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("createdAt").ascending());
        GroupOverviewDto overviewDto;

        if (page == 1) {
            overviewDto = overviewService.retrievedOverview(groupId, userInfoDto, pageable);
        } else {
            overviewDto = new GroupOverviewDto();
            overviewService.updateRetrievedExpenseList(overviewDto, groupId, userInfoDto, pageable);
        }
        ResponseDto response = new ResponseDto<>(true, "GroupDetail retrieved successfully", overviewDto);
        return ResponseEntity.ok(response);
    }

}
