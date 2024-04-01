package settleup.backend.domain.user.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import settleup.backend.domain.user.entity.dto.UserInfoDto;

import java.util.List;

public interface SearchService {
    Page<UserInfoDto> getUserList(String partOfEmail, Pageable pageable);
}
