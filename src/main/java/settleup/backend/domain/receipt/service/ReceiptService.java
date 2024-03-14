package settleup.backend.domain.receipt.service;

import settleup.backend.domain.receipt.entity.dto.ReceiptDto;
import settleup.backend.domain.receipt.entity.dto.RequireTransactionDto;
import settleup.backend.global.exception.CustomException;

public interface ReceiptService {
    RequireTransactionDto createReceipt(ReceiptDto requestDto) throws CustomException;

    ReceiptDto getReceiptInfo (String ReceiptUUID) throws CustomException;
}
