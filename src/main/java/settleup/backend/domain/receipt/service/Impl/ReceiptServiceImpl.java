package settleup.backend.domain.receipt.service.Impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.receipt.entity.ReceiptEntity;
import settleup.backend.domain.receipt.entity.dto.ReceiptRequestDto;
import settleup.backend.domain.receipt.repository.ReceiptItemRepository;
import settleup.backend.domain.receipt.repository.ReceiptItemUserRepository;
import settleup.backend.domain.receipt.repository.ReceiptRepository;
import settleup.backend.domain.receipt.service.ReceiptService;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.exception.CustomException;
@Service
@Transactional
@AllArgsConstructor
public class ReceiptServiceImpl implements ReceiptService {

    private final ReceiptRepository receiptRepo;
    private final ReceiptItemRepository receiptItemRepo;
    private final ReceiptItemUserRepository receiptItemUserRepo;
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    @Override
    public Long saveReceiptCommonData(ReceiptRequestDto requestDto) throws CustomException {
        //유효성 대상
        ReceiptEntity receiptEntity = new ReceiptEntity();

        return null;
    }
}
