package settleup.backend.domain.receipt.service;

import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.domain.transaction.entity.dto.TransactionDto;
import settleup.backend.global.exception.CustomException;

import java.util.concurrent.CompletableFuture;

public interface ReceiptService {
    TransactionDto createReceipt(ReceiptDto requestDto) ;

    ReceiptDto getReceiptInfo (String ReceiptUUID) throws CustomException;
}
