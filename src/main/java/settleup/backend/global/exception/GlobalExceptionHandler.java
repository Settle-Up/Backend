package settleup.backend.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import settleup.backend.global.api.ResponseDto;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto> handleCustomException(CustomException e) {
        ResponseDto<Void> response = new ResponseDto<>(
                false,
                e.getMessage() + (e.getDetailMessage() != null ? ": " + e.getDetailMessage() : ""),
                null,
                e.getSimpleErrorCode()
        );

        HttpStatus status = ErrorHttpStatusMapping.mapErrorCodeToHttpStatus(e.getErrorCode());
        return new ResponseEntity<>(response, status);
    }
}
