package settleup.backend.domain.transaction.service;

import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.domain.transaction.entity.dto.TransactionP2PResultDto;
import settleup.backend.global.exception.CustomException;


public interface OptimizedService extends TransactionProcessingService{

    TransactionP2PResultDto optimizationOfp2p(TransactionDto targetDto) throws CustomException;

}
