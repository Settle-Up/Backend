package settleup.backend.domain.receipt.service;

import settleup.backend.domain.receipt.entity.dto.ReceiptRequestDto;
import settleup.backend.global.exception.CustomException;

public interface ReceiptService {
    Long saveReceiptCommonData(ReceiptRequestDto requestDto)throws CustomException;
}
