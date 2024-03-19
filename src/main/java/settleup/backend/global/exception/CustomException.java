package settleup.backend.global.exception;


public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detailMessage;

    // 누락된 필드 정보를 담는 생성자 추가
    public CustomException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getDescription());
        this.errorCode = errorCode;
        this.detailMessage = detailMessage;
    }

    // 기존 생성자도 유지
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

    public String getErrorCodeName() {
        return errorCode.name();
    }
}
