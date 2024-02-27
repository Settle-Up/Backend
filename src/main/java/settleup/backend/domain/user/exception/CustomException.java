package settleup.backend.domain.user.exception;



public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }


    public String getSimpleErrorCode() {
        return String.valueOf(errorCode.getCode());
    }


    public String getErrorCodeName() {
        return errorCode.name();
    }
}

