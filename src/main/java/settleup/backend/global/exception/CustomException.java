package settleup.backend.global.exception;


public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detailMessage;

    public CustomException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    public CustomException(ErrorCode errorCode) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.detailMessage = null;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDetailMessage() {
        return detailMessage;
    }

    public String getSimpleErrorCode() {
        return String.valueOf(errorCode.getCode());
    }

}
