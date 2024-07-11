package settleup.backend.domain.transaction.entity;

import settleup.backend.domain.group.entity.AbstractGroupEntity;
import settleup.backend.domain.user.entity.AbstractUserEntity;

import settleup.backend.global.Helper.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionalEntity {
    Long getId();

    AbstractUserEntity getSenderUser();

    AbstractUserEntity getRecipientUser();

    AbstractGroupEntity getGroup();

    BigDecimal getTransactionAmount();

    String getTransactionUUID();

    Boolean getHasBeenSent();

    Boolean getHasBeenChecked();

    Status getRequiredReflection();

    LocalDateTime getCreatedAt();


    LocalDateTime getClearStatusTimeStamp();


    Status getUserType();

}
