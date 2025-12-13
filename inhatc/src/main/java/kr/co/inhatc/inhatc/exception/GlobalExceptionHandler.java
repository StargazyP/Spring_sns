package kr.co.inhatc.inhatc.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.servlet.http.HttpServletRequest;
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
     * 이미지 요청인 경우 적절한 Content-Type으로 응답
     */
    @ExceptionHandler(java.io.IOException.class)
    protected ResponseEntity<?> handleIOException(final java.io.IOException e, HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        
        // Broken Pipe 에러는 클라이언트가 연결을 끊은 정상적인 상황
        if (e.getMessage() != null && e.getMessage().contains("Broken pipe")) {
            log.debug("Client aborted connection: {}", requestPath);
            return ResponseEntity.status(HttpStatus.OK).build();
        }
        
        // 이미지 요청인 경우
        if (isImageRequest(requestPath, acceptHeader)) {
            log.debug("이미지 리소스를 찾을 수 없음: {}", requestPath);
            // 이미지 요청에 대한 에러는 빈 바이트 배열 반환
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.IMAGE_PNG)
                    .body(new byte[0]);
        }
        
        // 일반 요청인 경우 JSON 에러 응답
        log.warn("IOException 발생: {} - {}", requestPath, e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ErrorResponse(ErrorCode.INTERNAL_SERVER_ERROR));
    }
    
    /**
     * 이미지 요청인지 확인
     */
    private boolean isImageRequest(String path, String acceptHeader) {
        if (path == null) return false;
        
        // 경로로 확인
        boolean isImagePath = path.endsWith(".png") || path.endsWith(".jpg") || 
                             path.endsWith(".jpeg") || path.endsWith(".gif") ||
                             path.endsWith(".webp") || path.endsWith(".svg");
        
        // Accept 헤더로 확인
        boolean isImageAccept = acceptHeader != null && acceptHeader.contains("image/");
        
        return isImagePath || isImageAccept;
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
        } else if (resourcePath != null && 
                   (resourcePath.endsWith(".png") || resourcePath.endsWith(".jpg") || 
                    resourcePath.endsWith(".jpeg") || resourcePath.endsWith(".gif") ||
                    resourcePath.contains("profile.png"))) {
            // 프로필 이미지나 이미지 리소스는 DEBUG 레벨로 처리 (정상적인 상황)
            log.debug("정적 리소스를 찾을 수 없음: {}", resourcePath);
        } else {
            // 실제 중요한 리소스인 경우 WARN 레벨로 처리
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
