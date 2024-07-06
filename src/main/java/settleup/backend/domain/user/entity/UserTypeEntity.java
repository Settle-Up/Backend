package settleup.backend.domain.user.entity;

import settleup.backend.global.Helper.Status;

public interface UserTypeEntity {
    Long getId();
    String getUserUUID();
    String getUserName();
    String getUserPhone();
    String getUserEmail();
    Boolean getIsDecimalInputOption();
    void setUserUUID(String userUUID);
    void setUserName(String userName);
    void setUserPhone(String userPhone);
    void setUserEmail(String userEmail);
    void setIsDecimalInputOption(Boolean isDecimalInputOption);
    Status getUserType();
}
