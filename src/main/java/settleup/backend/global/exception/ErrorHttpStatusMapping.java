package settleup.backend.global.exception;

import org.springframework.http.HttpStatus;

public class ErrorHttpStatusMapping {

    public static HttpStatus mapErrorCodeToHttpStatus(ErrorCode errorCode) {

        switch (errorCode) {
            case USER_NOT_FOUND:
                return HttpStatus.NOT_FOUND;
            case DATABASE_ERROR:
            case REGISTRATION_FAILED:
            case GROUP_CREATION_FAILED:
                return HttpStatus.INTERNAL_SERVER_ERROR;
            case SIGNUP_ERROR:
            case AUTH_ERROR:
            case PASSWORD_FORMAT_INVALID:
            case USER_ID_DUPLICATE:
            case USER_ID_FORMAT_INVALID:
            case INVALID_INPUT:
                return HttpStatus.BAD_REQUEST;
            case PARSE_ERROR:
                return HttpStatus.UNPROCESSABLE_ENTITY;
            case TOKEN_EXPIRED:
            case TOKEN_MALFORMED:
            case TOKEN_INVALID:
            case TOKEN_WRONG_SUBJECT:
                return HttpStatus.UNAUTHORIZED;
            case EXTERNAL_API_ERROR_SOCIAL_TOKEN:
            case EXTERNAL_API_ERROR:
            case EXTERNAL_API_EMPTY_RESPONSE:
                return HttpStatus.SERVICE_UNAVAILABLE;
            case TOKEN_CREATION_FAILED:
                return HttpStatus.FORBIDDEN;
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}

