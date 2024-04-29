package settleup.backend.domain.user.service;

import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;

import java.util.Map;

public interface UserService {
    Map<String,String> clusterUserDecimal(UserInfoDto userInfoDto)throws CustomException;
    Map<String, String> retrievedUserDecimal(UserInfoDto userInfoDto) throws  CustomException;
}
