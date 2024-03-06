package settleup.backend.global.exception;


public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getSimpleErrorCode() {
        return String.valueOf(errorCode.getCode());
    }


    public String getErrorCodeName() {
        return errorCode.name();
    }
}

