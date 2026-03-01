package velog.velog.system.security.jwt.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;
import velog.velog.system.exception.dto.ErrorResponse;
import velog.velog.system.exception.model.ErrorCode;
import velog.velog.system.security.jwt.exception.JwtBlacklistException;
import velog.velog.system.security.jwt.exception.JwtRestException;
import velog.velog.system.security.jwt.util.JwtTokenProvider;
import velog.velog.system.security.util.CookieUtils;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final CookieUtils cookieUtils;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            // 1. 쿠키에서 ATK 추출
            String token = cookieUtils.getAccessTokenFromRequest(request);

            if (token != null && jwtTokenProvider.validateToken(token)) {
                // 2. Redis 블랙리스트 체크
                Boolean isBlacklisted = redisTemplate.hasKey("BLACKLIST_ATK:" + token);
                if (isBlacklisted != null && isBlacklisted) {
                    throw new JwtBlacklistException(); // 블랙리스트 발견 시 예외 발생
                }

                Authentication auth = jwtTokenProvider.getAuthentication(token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
            filterChain.doFilter(request, response);

        } catch (JwtRestException e) {
            sendErrorResponse(response, e.getErrorCode());
        }
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorBody = ErrorResponse.builder()
                .status(errorCode.getStatus().value())
                .error(errorCode.getError())
                .message(errorCode.getMessage())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}