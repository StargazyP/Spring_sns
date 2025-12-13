package kr.co.inhatc.inhatc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /*
     * Developer Custom Exception
     */
    @ExceptionHandler(CustomException.class)
    protected ResponseEntity<ErrorResponse> handleCustomException(final CustomException e) {
        log.error("handleCustomException: {}", e.getErrorCode());
        return ResponseEntity
                .status(e.getErrorCode().getStatus().value())
                .body(new ErrorResponse(e.getErrorCode()));
    }

    /*
     * HTTP 405 Exception
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(final HttpRequestMethodNotSupportedException e) {
        log.error("handleHttpRequestMethodNotSupportedException: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.METHOD_NOT_ALLOWED.getStatus().value())
                .body(new ErrorResponse(ErrorCode.METHOD_NOT_ALLOWED));
    }

    /*
     * IOException 처리
     */
    @ExceptionHandler(java.io.IOException.class)
    protected ResponseEntity<ErrorResponse> handleIOException(final java.io.IOException e) {
        log.error("IOException 발생: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR));
    }

    /*
     * Bean Validation 오류 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(final MethodArgumentNotValidException e) {
        StringBuilder errorMessage = new StringBuilder("입력값 검증 실패: ");
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errorMessage.append(error.getField()).append(" - ").append(error.getDefaultMessage()).append("; ");
        }
        log.warn("입력값 검증 실패: {}", errorMessage.toString());
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus().value())
                .body(new ErrorResponse(ErrorCode.BAD_REQUEST));
    }

    /*
     * IllegalArgumentException 처리 (파일 검증 실패 등)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<ErrorResponse> handleIllegalArgumentException(final IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage());
        return ResponseEntity
                .status(ErrorCode.BAD_REQUEST.getStatus().value())
                .body(new ErrorResponse(ErrorCode.BAD_REQUEST));
    }

    /*
     * 정적 리소스를 찾을 수 없는 경우 (favicon.ico, .well-known 등)
     * 이는 정상적인 경우이므로 WARN 레벨로 처리
     */
    @ExceptionHandler(NoResourceFoundException.class)
    protected ResponseEntity<Void> handleNoResourceFoundException(final NoResourceFoundException e) {
        String resourcePath = e.getResourcePath();
        // favicon.ico나 .well-known 같은 브라우저 자동 요청은 DEBUG 레벨로 처리
        if (resourcePath != null && 
            (resourcePath.contains("favicon.ico") || 
             resourcePath.contains(".well-known") ||
             resourcePath.contains("robots.txt"))) {
            log.debug("정적 리소스를 찾을 수 없음 (무시 가능): {}", resourcePath);
        } else {
            // 실제 이미지나 중요한 리소스인 경우 WARN 레벨로 처리
            log.warn("정적 리소스를 찾을 수 없음: {}", resourcePath);
        }
        return ResponseEntity.notFound().build();
    }

    /*
     * HTTP 500 Exception
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(final Exception e) {
        log.error("예상치 못한 예외 발생: {}", e.getMessage(), e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus().value())
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR));
    }

}
