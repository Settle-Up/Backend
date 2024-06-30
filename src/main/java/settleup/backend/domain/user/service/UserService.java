package settleup.backend.domain.user.service;

import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;

import java.util.Map;
import java.util.Objects;

public interface UserService {
    Map<String, Object> clusterUserDecimal(UserInfoDto userInfoDto)throws CustomException;
    Map<String, Object> retrievedUserDecimal(UserInfoDto userInfoDto) throws  CustomException;
}
