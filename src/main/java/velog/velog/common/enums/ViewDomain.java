package velog.velog.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ViewDomain {
    POST("post");

    private final String prefix;
}
