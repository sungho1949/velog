package velog.velog.system.exception.model;

import lombok.Getter;

@Getter
public class RestException extends RuntimeException {
    private final ErrorCode errorCode;

    public RestException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public RestException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
