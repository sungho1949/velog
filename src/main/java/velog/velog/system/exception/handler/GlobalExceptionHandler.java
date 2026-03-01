package velog.velog.system.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import velog.velog.system.exception.dto.ErrorResponse;
import velog.velog.system.exception.model.ErrorCode;
import velog.velog.system.exception.model.RestException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. 커스텀 예외 처리 (RestException)
    @ExceptionHandler(RestException.class)
    public ResponseEntity<ErrorResponse> handleRestException(RestException e) {
        log.warn("🚨 [RestException] Code: {}, Message: {}", e.getErrorCode().getError(), e.getMessage());
        return ErrorResponse.toResponseEntity(e.getErrorCode(), e.getMessage());
    }

    // 2. @Valid 유효성 검사 실패 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        var fieldError = e.getBindingResult().getFieldErrors().get(0);
        String errorMessage = String.format("[%s] %s", fieldError.getField(), fieldError.getDefaultMessage());

        log.warn("❌ [Validation Failed] {}", errorMessage);
        return ErrorResponse.toResponseEntity(ErrorCode.GLOBAL_INVALID_PARAMETER, errorMessage);
    }

    // 3. DB 제약 조건 위반 처리
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        String msg = e.getMessage();
        log.error("💾 [DB Violation] {}", msg);

        if (msg != null) {
            // 중복 제약 조건 (Unique Key)
            if (msg.contains("Duplicate entry")) {
                if (msg.contains("email") || msg.contains("USER_EMAIL")) {
                    return ErrorResponse.toResponseEntity(ErrorCode.AUTH_EMAIL_ALREADY_EXISTS);
                }
                return ErrorResponse.toResponseEntity(ErrorCode.GLOBAL_BAD_REQUEST, "이미 존재하는 데이터입니다.");
            }

            // NOT NULL 제약 조건
            if (msg.contains("cannot be null") || msg.contains("Column") && msg.contains("not-null")) {
                return ErrorResponse.toResponseEntity(ErrorCode.DB_NOT_NULL_VIOLATION);
            }
        }

        return ErrorResponse.toResponseEntity(ErrorCode.GLOBAL_BAD_REQUEST, "데이터 무결성 제약 조건을 위반했습니다.");
    }

    // 4. 나머지 모든 예외 처리 (서버 내부 오류)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e, HttpServletRequest request) {
        log.error("🔥 [Internal Server Error] Method: {}, URL: {}, Message: {}",
                request.getMethod(), request.getRequestURI(), e.getMessage(), e);

        return ErrorResponse.toResponseEntity(ErrorCode.GLOBAL_INTERNAL_SERVER_ERROR);
    }
}