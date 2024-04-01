package settleup.backend.global.exception;

public enum ErrorCode {
    USER_NOT_FOUND(100, "USER NOT FOUND"),
    DATABASE_ERROR(102, "DATABASE ERROR OCCURRED"),
    SIGNUP_ERROR(103, "SIGNUP ERROR OCCURRED"),
    AUTH_ERROR(104, "AUTH ERROR OCCURRED"),
    PASSWORD_FORMAT_INVALID(105, "PASSWORD FORMAT IS INVALID"),
    USER_ID_DUPLICATE(106, "USERID ALREADY EXISTS"),
    USER_ID_FORMAT_INVALID(107, "USERID FORMAT IS INVALID"),
    PARSE_ERROR(108, "RESPONSE BODY PARSE ERROR"),
    TOKEN_EXPIRED(109, "TOKEN EXPIRED"),
    TOKEN_MALFORMED(110, "TOKEN FORMAT IS INVALID"),
    TOKEN_INVALID(111, "TOKEN IS INVALID"),
    TOKEN_WRONG_SUBJECT(112, "NOT OUR SITE TOKEN"),
    EXTERNAL_API_ERROR_SOCIAL_TOKEN(113, "FAILED TO GET SOCIAL EXTERNAL API ACCESS TOKEN"),
    EXTERNAL_API_ERROR(114, "FAILED TO RETRIEVE USER INFO FROM KAKAO"),
    EXTERNAL_API_EMPTY_RESPONSE(115, "SOCIAL EXTERNAL API, USER INFO RESPONSE IS EMPTY"),
    REGISTRATION_FAILED(116, "USER REGISTRATION FAILED, ERRORS OCCURRED DURING UUID GENERATION AND SAVE DB"),
    TOKEN_CREATION_FAILED(117, "FAILED TO CREATE LOGIN TOKEN"),
    INVALID_INPUT(118, "INVALID INPUT PROVIDED"),
    GROUP_CREATION_FAILED(119, "ERRORS OCCURRED DURING UUID GENERATION, URL GENERATION, OR DB SAVING"),
    ENCODING_ERROR(120, "FAILED TO CONVERT FILE TO BASE64"),
    FILE_PROCESSING_ERROR(121, "FAILED TO READ FILE CONTENT"),
    OPERATION_OCR_RESPONSE_STATUS_ERROR(122, "OPERATION FAILED WITH STATUS DURING AZURE API RESPONSE"),
    RECEIVED_UNEXPECTED_STATUS(123, "RECEIVED UNEXPECTED STATUS"),
    EXTERNAL_OCR_API_ERROR(124, "EXCEPTION OCCURRED WHILE PROCESSING THE AZURE_API RESPONSE"),
    ERROR_CREATION_TOKEN_IN_SERVER_A(125, "ERROR WHILE ENCRYPTING PRIVATE USERINFO"),
    ERROR_PARSE_TOKEN_IN_SERVER(126, "ERROR WHILE DECRYPTING PRIVATE USERINFO"),
    ERROR_CREATION_TOKEN_IN_SERVER_B(127, "ERROR WHILE PRIVATE USERINFO ENCRYPTING UN SUPPORT ENCODING"),
    GROUP_NOT_FOUND(128, "GROUP NOT FOUND"),
    TOTAL_AMOUNT_ERROR(129, "TOTAL SAVE AMOUNT DOES NOT MATCH THE ACTUAL PAID PRICE."),
    RECEIPT_NOT_FOUND(130, "RECEIPT CANNOT FIND"),
    GROUP_USER_NOT_FOUND(131, "USER CANNOT FIND IN GROUP"),
    PAYER_USER_NOT_FOUND(132, "PAYER USER CANNOT FOUND"),
    OWED_USER_NOT_FOUND(133, "OWED USER CANNOT FOUND"),
    OPTIMIZATION_ERROR(134, "ERROR DURING OPTIMIZATION"),
    SETTLED_REQUIRED(135, "USER SETTLED REQUIRED"),
    TRANSACTION_ID_NOT_FOUND_IN_GROUP(136, "TRANSACTION ID CANNOT BE FOUND IN GROUP"),
    TRANSACTION_TYPE_NOT_SUPPORTED(137,"TRANSACTION TYPE NOT SUPPORTED");




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
