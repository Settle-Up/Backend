package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.NetDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.user.entity.dto.UserGroupDto;

import java.util.List;

public interface NetService {
    List<NetDto> calculateNet(UserGroupDto groupDto);
}
