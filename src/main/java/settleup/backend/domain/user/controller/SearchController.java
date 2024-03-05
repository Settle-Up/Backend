package settleup.backend.domain.user.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;
import settleup.backend.domain.user.service.LoginService;
import settleup.backend.domain.user.service.SearchService;
import settleup.backend.global.api.ResponseDto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@AllArgsConstructor
@RequestMapping("/home")
public class SearchController {

    private SearchService searchService;
    private LoginService loginService;

    @GetMapping("")
    public ResponseEntity<ResponseDto> findUserEmail(
            @RequestHeader(value = "Authorization") String token, @RequestParam("search") String partOfEmail) {
        loginService.validTokenOrNot(token);
        List<UserInfoDto> userInfoDto = searchService.getUserList(partOfEmail);
        Map<String, Object> data = new HashMap<>();
        data.put("SearchList", userInfoDto);
        ResponseDto<Map<String, Object>> responseDto;
        responseDto = new ResponseDto<>(true, "retrieved successfully", data, null);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }
}