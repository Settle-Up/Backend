package settleup.backend.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import settleup.backend.global.common.ResponseDto;

import javax.net.ssl.SSLException;

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

    @ExceptionHandler(SSLException.class)
    public String handleSSLException(SSLException e, Model model) {
        model.addAttribute("errorMessage", "SSL 인증서 오류가 발생했습니다: " + e.getMessage());
        return "errorPage";
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleGeneralException(Exception e) {
        ResponseDto<Void> response = new ResponseDto<>(
                false,
                "General Error: " + e.getMessage(),
                null,
                "GENERAL_ERROR"
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}