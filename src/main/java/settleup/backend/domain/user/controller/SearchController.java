package settleup.backend.domain.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.domain.user.service.SearchService;
import settleup.backend.global.Helper.ResponseDto;

import java.util.HashMap;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/users/search")
public class SearchController {

    private SearchService searchService;
    private LoginService loginService;


    @GetMapping("")
    public ResponseEntity<ResponseDto<Map<String, Object>>> findUserEmail(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam("email") String partOfEmail,
            @RequestParam(value = "excludeGroupId", required = false) String groupId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("userEmail").ascending());

        Page<UserInfoDto> userInfoPage;
        if (groupId == null || groupId.isEmpty()) {
            userInfoPage = searchService.getUserList(partOfEmail, pageable, userInfoDto);
        } else {
            userInfoPage = searchService.getUserListNotIncludeGroupUser(partOfEmail, pageable, userInfoDto, groupId);
        }

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("hasNextPage", userInfoPage.hasNext());
        responseData.put("searchList", userInfoPage.getContent());

        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>(
                true,
                "retrieved successfully",
                responseData);
        return ResponseEntity.ok(responseDto);
    }
}