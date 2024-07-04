package settleup.backend.global.exception;

import io.sentry.Sentry;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import settleup.backend.global.common.ResponseDto;

import javax.net.ssl.SSLException;
import jakarta.servlet.ServletException; // 추가된 부분

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
    public ResponseEntity<ResponseDto> handleSSLException(SSLException e) {
        Sentry.captureException(e); // Sentry로 예외 전송
        ResponseDto<Void> response = new ResponseDto<>(
                false,
                "SSL 인증서 오류가 발생했습니다: " + e.getMessage(),
                null,
                "SSL_ERROR"
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto> handleGeneralException(Exception e) {
        Sentry.captureException(e); // Sentry로 예외 전송
        ResponseDto<Void> response = new ResponseDto<>(
                false,
                "Error: " + e.getMessage(), // 구체적인 예외 메시지 전달
                null,
                "GENERAL_ERROR"
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
