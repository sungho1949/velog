package velog.velog.domain.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import velog.velog.domain.auth.dto.AuthDto;
import velog.velog.domain.auth.dto.EmailPurpose;
import velog.velog.domain.auth.service.AuthService;
import velog.velog.domain.auth.service.EmailService;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmailService emailService;
    private final AuthService authService;

    @PostMapping("/email/send")
    public ResponseEntity<String> sendEmail(@RequestParam String email, @RequestParam EmailPurpose purpose) {
        emailService.sendVerificationCode(email, purpose);
        return ResponseEntity.accepted().body("인증번호 발송.");
    }

    @PostMapping("/email/verify")
    public ResponseEntity<String> verifyEmail(@RequestBody @Valid AuthDto.VerifyEmailRequest request) {
        emailService.verifyCode(request);
        return ResponseEntity.ok("인증 성공.");
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid AuthDto.SignUpRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDto.LoginResponse> login(@RequestBody @Valid AuthDto.LoginRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request, HttpServletResponse response) {
        authService.logout(userDetails.getUsername(), request, response);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> delete(@AuthenticationPrincipal UserDetails userDetails, HttpServletResponse response) {
        authService.delete(userDetails.getUsername(), response);
        return ResponseEntity.ok("회원 탈퇴 완료.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthDto.LoginResponse> refresh(@RequestBody String refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }

    @GetMapping("/email-exist")
    public ResponseEntity<Boolean> checkEmail(@RequestParam String email) {
        return ResponseEntity.ok(authService.checkEmailDuplicate(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid AuthDto.ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok("비밀번호가 성공적으로 재설정되었습니다. 다시 로그인해주세요.");
    }
}
