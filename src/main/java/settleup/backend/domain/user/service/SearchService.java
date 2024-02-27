package settleup.backend.domain.user.service;

import settleup.backend.domain.user.entity.dto.UserInfoDto;

import java.util.List;

public interface SearchService {
    List<UserInfoDto> getUserList(String partOfEmail);
}
