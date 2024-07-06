package settleup.backend.domain.transaction.model;

import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.group.entity.GroupTypeEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.domain.user.entity.UserTypeEntity;
import settleup.backend.global.Helper.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionalEntity {
    Long getId();

    UserTypeEntity getSenderUser();

    UserTypeEntity getRecipientUser();

    GroupTypeEntity getGroup();

    BigDecimal getTransactionAmount();

    String getTransactionUUID();

    Boolean getHasBeenSent();

    Boolean getHasBeenChecked();

    Status getRequiredReflection();

    LocalDateTime getCreatedAt();


    LocalDateTime getClearStatusTimeStamp();


    Status getUserType();

}
