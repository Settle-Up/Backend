package settleup.backend.domain.group.service;

import settleup.backend.domain.group.entity.dto.CreateGroupRequestDto;
import settleup.backend.domain.group.entity.dto.CreateGroupResponseDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;

public interface ClusterService {
    CreateGroupResponseDto createGroup(CreateGroupRequestDto requestDto)throws CustomException;

}
