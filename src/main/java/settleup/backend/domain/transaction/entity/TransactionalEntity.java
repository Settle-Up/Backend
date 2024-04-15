package settleup.backend.domain.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import settleup.backend.domain.group.entity.GroupEntity;
import settleup.backend.domain.user.entity.UserEntity;
import settleup.backend.global.common.Status;

import java.time.LocalDateTime;

public interface TransactionalEntity {
    Long getId();

    UserEntity getSenderUser();

    UserEntity getRecipientUser();

    GroupEntity getGroup();

    double getTransactionAmount();

    String getTransactionUUID();

    Boolean getHasBeenSent();

    Boolean getHasBeenChecked();

    Status getRequiredReflection();

    LocalDateTime getCreatedAt();


    LocalDateTime getClearStatusTimeStamp();

}
