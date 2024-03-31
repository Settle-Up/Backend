package settleup.backend.domain.transaction.service.Impl;

import settleup.backend.domain.transaction.entity.TransactionalEntity;
import settleup.backend.domain.transaction.service.TransactionProcessingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.repository.GroupRepository;
import settleup.backend.domain.group.repository.GroupUserRepository;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateDto;
import settleup.backend.domain.transaction.entity.dto.TransactionUpdateRequestDto;
import settleup.backend.domain.transaction.service.TransactionUpdateService;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.dto.UserInfoDto;
import settleup.backend.domain.user.repository.UserRepository;
import settleup.backend.global.common.Status;
import settleup.backend.global.exception.CustomException;
import settleup.backend.global.exception.ErrorCode;

import java.util.ArrayList;


@Service
@AllArgsConstructor
@Transactional
public class TransactionUpdateServiceImpl implements TransactionUpdateService {
    private final UserRepository userRepo;
    private final GroupRepository groupRepo;
    private final GroupUserRepository groupUserRepo;
    private final TransactionStrategySelector strategySelector;


 @Override
    public TransactionUpdateDto transactionUpdate(UserInfoDto userInfoDto, String groupId, TransactionUpdateRequestDto request) throws CustomException {
        UserEntity existingUser = userRepo.findByUserUUID(userInfoDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        GroupEntity existingGroup = groupRepo.findByGroupUUID(groupId)
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_NOT_FOUND));
        groupUserRepo.findByUserIdAndGroupId(existingUser.getId(), existingGroup.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.GROUP_USER_NOT_FOUND));

        String transactionIdentity = request.getTransactionId();

        TransactionProcessingService processingService = strategySelector.selectService(transactionIdentity);
        String afterApprovalTransactionId = processingService.processTransaction(transactionIdentity, request, existingGroup);

                TransactionalEntity result =strategySelector.selectRepository(afterApprovalTransactionId);
                TransactionUpdateDto transactionUpdateDto = new TransactionUpdateDto();
                transactionUpdateDto.setUserId(existingUser.getUserUUID());
                transactionUpdateDto.setUserName(existingUser.getUserName());


                UserEntity counterParty;
                Status transactionDirection;
                Status isRejected;

                if ("sender".equals(request.getApprovalUser())) {
                    transactionDirection = Status.OWE;
                    counterParty= result.getRecipientUser();
                    isRejected=result.getIsRecipientStatus();
                } else {
                    transactionDirection = Status.OWED;
                    counterParty= result.getSenderUser();
                    isRejected=result.getIsSenderStatus();
                }

                if (transactionUpdateDto.getTransactionUpdateList() == null) {
                    transactionUpdateDto.setTransactionUpdateList(new ArrayList<>());
                }


                TransactionUpdateDto.TransactionListDto transactionListDto = new TransactionUpdateDto.TransactionListDto();
                transactionListDto.setTransactionId(result.getTransactionUUID());
                transactionListDto.setCounterPartyId(counterParty.getUserUUID());
                transactionListDto.setCounterPartyName(counterParty.getUserName());
                transactionListDto.setTransactionDirection(String.valueOf(transactionDirection));
                transactionListDto.setHasSentOrReceived(request.getApprovalStatus());
                transactionListDto.setIsRejected(String.valueOf(isRejected));
                transactionListDto.setTransactionAmount(String.valueOf(result.getTransactionAmount()));

                transactionUpdateDto.getTransactionUpdateList().add(transactionListDto);
                return transactionUpdateDto;
            }
        }


