package settleup.backend.domain.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.domain.user.service.SearchService;
import settleup.backend.global.common.ResponseDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@AllArgsConstructor
@RequestMapping("")
public class SearchController {

    private SearchService searchService;
    private LoginService loginService;


    @GetMapping("")
    public ResponseEntity<ResponseDto<Map<String, Object>>> findUserEmail(
            @RequestHeader(value = "Authorization") String token,
            @RequestParam("search") String partOfEmail,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        UserInfoDto userInfoDto = loginService.validTokenOrNot(token);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("userEmail").ascending());
        Page<UserInfoDto> userInfoPage = searchService.getUserList(partOfEmail, pageable, userInfoDto);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("hasNextPage", userInfoPage.hasNext());
        responseData.put("searchList", userInfoPage.getContent());

        ResponseDto<Map<String, Object>> responseDto = new ResponseDto<>(
                true,
                "retrieved successfully",
                responseData
        );

        return ResponseEntity.ok(responseDto);
    }

}