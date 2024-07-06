package settleup.backend.global.exception;

import org.springframework.http.HttpStatus;

public class ErrorHttpStatusMapping {

    public static HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {
        switch (errorCode) {
            case USER_NOT_FOUND:
            case GROUP_NOT_FOUND:
            case RECEIPT_NOT_FOUND:
            case GROUP_USER_NOT_FOUND:
            case PAYER_USER_NOT_FOUND:
            case OWED_USER_NOT_FOUND:
            case TRANSACTION_ID_NOT_FOUND_IN_GROUP:
                return HttpStatus.NOT_FOUND;

            case DATABASE_ERROR:
            case REGISTRATION_FAILED:
            case GROUP_CREATION_FAILED:
            case ENCODING_ERROR:
            case FILE_PROCESSING_ERROR:
            case RECEIVED_UNEXPECTED_STATUS:
            case ERROR_CREATION_TOKEN_IN_SERVER_A:
            case ERROR_PARSE_TOKEN_IN_SERVER:
            case ERROR_CREATION_TOKEN_IN_SERVER_B:
            case OPTIMIZATION_ERROR:
            case TOKEN_CREATION_FAILED:
            case TRANSACTION_TYPE_NOT_SUPPORTED:
                return HttpStatus.INTERNAL_SERVER_ERROR;

            case SIGNUP_ERROR:
            case AUTH_ERROR:
            case PASSWORD_FORMAT_INVALID:
            case USER_ID_DUPLICATE:
            case USER_ID_FORMAT_INVALID:
            case INVALID_INPUT:
            case TOTAL_AMOUNT_ERROR:
                return HttpStatus.BAD_REQUEST;

            case PARSE_ERROR:
                return HttpStatus.UNPROCESSABLE_ENTITY;

            case TOKEN_EXPIRED:
            case TOKEN_MALFORMED:
            case TOKEN_INVALID:
            case TOKEN_WRONG_ISSUER:
                return HttpStatus.UNAUTHORIZED;

            case EXTERNAL_API_ERROR_SOCIAL_TOKEN:
            case EXTERNAL_API_ERROR:
            case EXTERNAL_API_EMPTY_RESPONSE:
            case EXTERNAL_OCR_API_ERROR:
            case OPERATION_OCR_RESPONSE_STATUS_ERROR:
                return HttpStatus.SERVICE_UNAVAILABLE;

            case SETTLED_REQUIRED:
                return HttpStatus.CONFLICT;

            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}

