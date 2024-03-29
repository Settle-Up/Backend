package settleup.backend.domain.transaction.entity;

import settleup.backend.domain.user.entity.UserEntity;

import java.time.LocalDateTime;

public interface TransactionalEntity {
    Long getId();
    UserEntity getSenderUser();
    UserEntity getRecipientUser();

    double getTransactionAmount();
    String getTransactionUUID();
    LocalDateTime getClearStatusTimeStamp();

}
