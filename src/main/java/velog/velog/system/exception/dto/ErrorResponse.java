package velog.velog.system.exception.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.ResponseEntity;
import velog.velog.system.exception.model.ErrorCode;

import java.time.LocalDateTime;

@Getter
@Builder
public class ErrorResponse {
    private final String timestamp = LocalDateTime.now().toString();
    private final int status;
    private final String error;
    private final String message;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .error(errorCode.getError())
                        .message(errorCode.getMessage())
                        .build());
    }

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode,String message) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getStatus().value())
                        .error(errorCode.getError())
                        .message(message)
                        .build());
    }
}
