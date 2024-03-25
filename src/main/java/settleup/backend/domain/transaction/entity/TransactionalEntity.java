package settleup.backend.domain.transaction.entity;

import settleup.backend.domain.user.entity.UserEntity;

public interface TransactionalEntity {
    Long getId();
    UserEntity getSenderUser();
    UserEntity getRecipientUser();

    UserEntity getRecipient();
    double getTransactionAmount();
    String getTransactionUUID();
}
