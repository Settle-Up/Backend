package settleup.backend.domain.user.exception;

public enum ErrorCode {
    USER_NOT_FOUND(100, "User not found"),
    DATABASE_ERROR(102, "Database error occurred"),
    SIGNUP_ERROR(103, "Signup Error occurred"),
    AUTH_ERROR(104, "Auth Error occurred"),
    PASSWORD_FORMAT_INVALID(105, "Password format is invalid"),
    USER_ID_DUPLICATE(106, "UserId already exists"),
    USER_ID_FORMAT_INVALID(107, "UserId format is invalid"),
    PARSE_ERROR(108,"response body parse error"),
    TOKEN_EXPIRED(109,"token expired"),
    TOKEN_MALFORMED(110,"token format is invalid"),
    TOKEN_INVALID(111,"token is invalid"),
    TOKEN_WRONG_SUBJECT(112,"not our site token"),
    EXTERNAL_API_ERROR_TOKEN(113,"Failed to get social external api access token"),
    EXTERNAL_API_ERROR(114,"Failed to retrieve user info from Kakao"),
    EXTERNAL_API_EMPTY_RESPONSE(115,"social external api, user info response is empty"),
    REGISTRATION_FAILED(116, "User registration failed"),
    TOKEN_CREATION_FAILED(117, "Failed to create login token");

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
