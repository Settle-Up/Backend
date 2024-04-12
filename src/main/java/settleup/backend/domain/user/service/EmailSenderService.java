package settleup.backend.domain.user.service;

import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;

import java.util.List;
import java.util.Map;

public interface EmailSenderService {
    void sendEmailToNewGroupUser(CreateGroupResponseDto newUserGroupInfo)throws CustomException;
}
