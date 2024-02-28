package settleup.backend.domain.user.service;

import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;

public interface LoginService {
    UserInfoDto validTokenOrNot (String token) throws CustomException;

}
