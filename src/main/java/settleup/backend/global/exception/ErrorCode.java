package settleup.backend.global.exception;

public enum ErrorCode {
    USER_NOT_FOUND(100, "User not found"),
    DATABASE_ERROR(102, "Database error occurred"),
    SIGNUP_ERROR(103, "Signup Error occurred"),
    AUTH_ERROR(104, "Auth Error occurred"),
    PASSWORD_FORMAT_INVALID(105, "Password format is invalid"),
    USER_ID_DUPLICATE(106, "UserId already exists"),
    USER_ID_FORMAT_INVALID(107, "UserId format is invalid"),
    PARSE_ERROR(108, "response body parse error"),
    TOKEN_EXPIRED(109, "token expired"),
    TOKEN_MALFORMED(110, "token format is invalid"),
    TOKEN_INVALID(111, "token is invalid"),
    TOKEN_WRONG_SUBJECT(112, "not our site token"),
    EXTERNAL_API_ERROR_SOCIAL_TOKEN(113, "Failed to get social external api access token"),
    EXTERNAL_API_ERROR(114, "Failed to retrieve user info from Kakao"),
    EXTERNAL_API_EMPTY_RESPONSE(115, "social external api, user info response is empty"),
    REGISTRATION_FAILED(116, "User registration failed, Errors occurred during uuid generation and save db"),
    TOKEN_CREATION_FAILED(117, "Failed to create login token"),
    INVALID_INPUT(118, "Invalid input provided"),
    GROUP_CREATION_FAILED(119, "Errors occurred during uuid generation, url generation, or db saving"),
    ENCODING_ERROR(120, "Failed to convert file to base64,"),
    FILE_PROCESSING_ERROR(121, "Failed to read file content"),
    OPERATION_OCR_RESPONSE_STATUS_ERROR(122, "Operation failed with status during azure api response"),
    RECEIVED_UNEXPECTED_STATUS(123, "Received unexpected status"),
    EXTERNAL_OCR_API_ERROR(124, "Exception occurred while processing the Azure_API response"),
    ERROR_CREATION_TOKEN_IN_SERVER_A(125, "Error while encrypting private userInfo"),
    ERROR_PARSE_TOKEN_IN_SERVER(126,"Error while decrypting private userInfo" ),
    ERROR_CREATION_TOKEN_IN_SERVER_B(127,"Error while private userInfo encrypting un support encoding"),
    GROUP_NOT_FOUND(128, "Group not found"),
    TOTAL_AMOUNT_ERROR(129, "Total save amount does not match the actual paid price."),
    RECEIPT_NOT_FOUND(130,"receipt cannot find"),
    GROUP_USER_NOT_FOUND(131,"user cannot find in group"),
    PAYER_USER_NOT_FOUND(132,"payer user cannot found"),
    OWED_USER_NOT_FOUND(133, "owed user cannot found"),
    OPTIMIZATION_ERROR(134,"error during optimization");



    private final int code;
    private final String description;

    ErrorCode(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
