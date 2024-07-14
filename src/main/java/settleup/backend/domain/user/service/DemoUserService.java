package settleup.backend.domain.user.service;

import settleup.backend.domain.user.entity.dto.LoginDto;
import settleup.backend.global.exception.CustomException;

public interface DemoUserService {
    LoginDto createDemoToken(String user,String ip) throws CustomException;

    LoginDto retrieveDemoUserInfo(String token) throws CustomException;
}
