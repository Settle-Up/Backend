package settleup.backend.domain.group.service;

import org.springframework.data.domain.Pageable;
import settleup.backend.domain.group.entity.dto.GroupInfoListDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;

import java.util.List;

public interface RetrievedService {
    GroupInfoListDto getGroupInfoByUser(UserInfoDto userInfo, Pageable pageable) throws CustomException;

    List<UserInfoDto> getGroupUserInfo(String groupUUID,Boolean isRegularUser) throws  CustomException;
}
