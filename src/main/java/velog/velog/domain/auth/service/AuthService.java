package velog.velog.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import velog.velog.domain.auth.dto.AuthDto;
import velog.velog.domain.auth.dto.EmailPurpose;
import velog.velog.domain.user.dto.UserDto;
import velog.velog.domain.user.entity.User;
import velog.velog.domain.user.repository.UserRepository;
import velog.velog.system.security.jwt.dto.JwtDto;
import velog.velog.system.security.jwt.util.JwtTokenProvider;
import velog.velog.system.security.util.CookieUtils;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final CookieUtils cookieUtils;

    @Transactional
    public String register(AuthDto.SignUpRequest request) {
        // 1. Redis에서 인증 완료 여부 확인
        validateEmailVerified(request.getEmail(), EmailPurpose.SIGNUP);

        if(userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        if(!request.getPassword().equals(request.getPasswordCheck())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 2. User 엔티티 생성 및 저장
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .lastName(request.getLastName())
                .firstName(request.getFirstName())
                .build();

        userRepository.save(user);

        // 3. 가입 성공 시 삭제
        redisTemplate.delete("EMAIL_VERIFIED:SIGNUP:" + request.getEmail());
        return "회원가입이 완료되었습니다.";
    }

    @Transactional
    public AuthDto.LoginResponse login(AuthDto.LoginRequest request, HttpServletResponse response) {
        // 1. 이메일 존재 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 2. 비밀번호 일치 여부 확인
        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 3. 토큰 생성
        String atk = jwtTokenProvider.createAccessToken(user.getEmail());
        String rtk = jwtTokenProvider.createRefreshToken(user.getEmail());

        // 4. Refresh Token을 Redis에 저장
       redisTemplate.opsForValue().set("RTK:" + user.getEmail(), rtk, Duration.ofDays(14));

       // 쿠키
        cookieUtils.addAccessTokenCookie(response, atk, Duration.ofMinutes(30));
        cookieUtils.addRefreshTokenCookie(response, rtk, Duration.ofDays(14));

        // return
        return AuthDto.LoginResponse.of(
                UserDto.UserResponse.of(user),
                JwtDto.TokenExpiresInfo.of(atk, rtk, 30 * 60 * 1000L)
        );
    }

    @Transactional
    public void logout(String email, HttpServletRequest request, HttpServletResponse response) {
        // 1. Redis에서 rtk  삭제
        redisTemplate.delete("RTK:" + email);

        // 2. atk
        String atk = cookieUtils.getAccessTokenFromRequest(request);
        if (atk != null) {
            // 3. ATK 남은 유효시간 계산 (남은 시간만큼만 블랙리스트에 보관)
            long expiration = jwtTokenProvider.getExpiration(atk); // 토큰의 만료 시간(ms) 가져오기
            long now = System.currentTimeMillis();
            long remainTime = expiration - now;

            if (remainTime > 0) {
                // 4. Redis에 블랙리스트 등록
                redisTemplate.opsForValue().set(
                        "BLACKLIST_ATK:" + atk,
                        "LOGOUT",
                        Duration.ofMillis(remainTime)
                );
            }
        }

        cookieUtils.clearCookies(response);
    }

    @Transactional
    public void delete(String email, HttpServletResponse response) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        userRepository.delete(user);
        redisTemplate.delete("RTK:" + email);
        cookieUtils.clearAccessTokenCookie(response);
        cookieUtils.clearRefreshTokenCookie(response);
    }

    @Transactional
    public AuthDto.LoginResponse refresh(String refreshToken) {
        // 1. Refresh Token 유효성 검사
        if(!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("RTK가 만료되었거나 유효하지 않습니다.");
        }

        // 2. Token 추출
        String email = jwtTokenProvider.getEmail(refreshToken);

        // 3. Redis에서 해당 이메일의 RTK 조회
        String savedRefreshToken = redisTemplate.opsForValue().get("RTK:" + email);
        if(savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("로그인 세션이 만료되었거나 일치하지 않습니다.");
        }

        // 4. 새로운 토큰 생성
        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);

        // 5. Redis 업데이트
        redisTemplate.opsForValue().set(
                "RTK:" + email,
                newRefreshToken,
                Duration.ofDays(14)
        );

        // 6. 유저 정보 조회 및 응답 반환
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return AuthDto.LoginResponse.of(
                UserDto.UserResponse.of(user),
                JwtDto.TokenExpiresInfo.of(newAccessToken, newRefreshToken, 30 * 60 * 1000L)
        );
    }

    @Transactional
    public void resetPassword(AuthDto.ResetPasswordRequest request) {
        // 1. 비밀번호 일치 확인
        if (!request.getNewPassword().equals(request.getNewPasswordCheck())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 2. Redis에서 'PASSWORD_RESET'인지 확인
        validateEmailVerified(request.getEmail(), EmailPurpose.PASSWORD_RESET);

        // 3. 유저 존재 확인
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 4. 비밀번호 암호화 및 업데이트
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));

        redisTemplate.delete("EMAIL_VERIFIED:" + EmailPurpose.PASSWORD_RESET.name() + ":" + request.getEmail());
        redisTemplate.delete("RTK:" + request.getEmail());

    }

    @Transactional(readOnly = true)
    public boolean checkEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    private void validateEmailVerified(String email, EmailPurpose purpose) {
        String isVerified = redisTemplate.opsForValue().get("EMAIL_VERIFIED:" + purpose.name() + ":" + email);
        if (!"TRUE".equals(isVerified)) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다.");
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
