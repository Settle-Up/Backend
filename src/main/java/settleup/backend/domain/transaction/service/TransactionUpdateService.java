package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.TransactionUpdateDto;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateRequestDto;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.global.exception.CustomException;


public interface TransactionUpdateService {
    TransactionUpdateDto transactionUpdate(UserInfoDto userInfoDto, String groupId, TransactionUpdateRequestDto requestDto) throws CustomException;

    TransactionUpdateDto retrievedUpdateListInGroup(UserInfoDto userInfoDto) throws CustomException;
}
