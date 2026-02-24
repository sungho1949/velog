package velog.velog.system.security.jwt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class JwtDto {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TokenExpiresInfo {

        private String accessToken;
        private String refreshToken;
        private long accessTokenExpirationTime; // 만료 시간

        public static TokenExpiresInfo of(String atk, String rtk, long limit) {
            return TokenExpiresInfo.builder()
                    .accessToken(atk)
                    .refreshToken(rtk)
                    .accessTokenExpirationTime(limit)
                    .build();
        }
    }
}
