package settleup.backend.domain.receipt.service;

import settleup.backend.domain.receipt.entity.dto.ReceiptRequestDto;
import settleup.backend.domain.receipt.entity.dto.RequireTransactionDto;
import settleup.backend.global.exception.CustomException;

public interface ReceiptService {
    RequireTransactionDto createReceipt(ReceiptRequestDto requestDto) throws CustomException;

//    boolean checkUserAndGroupInfoBeforeSave(ReceiptRequestDto requestDto) throws CustomException;
}
