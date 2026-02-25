package velog.velog.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import velog.velog.domain.auth.dto.AuthDto;
import velog.velog.domain.auth.dto.EmailPurpose;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Async("mailTaskExecutor")
    public void sendVerificationCode(String email, EmailPurpose purpose) {
        // 인증 번호 랜덤으로 생성 10000~999999
        String code = String.format("%06d", secureRandom.nextInt(1000000));

        // Redis에 저장(key: email, value: code, 5분)
        String key = "EMAIL_CODE:" + purpose.name() + ":" + email;
        redisTemplate.opsForValue().set(key, code, Duration.ofMinutes(5));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("thdtjdgh1949@gmail.com");
        message.setTo(email);
        message.setSubject("[Velog] 회원가입 인증번호 - " + purpose);
        message.setText("인증번호 6자리: " + code);
        mailSender.send(message);
    }

    public void verifyCode(AuthDto.VerifyEmailRequest request) {
        String key = "EMAIL_CODE:" + request.getPurpose().name() + ":" + request.getEmail();
        String savedCode = redisTemplate.opsForValue().get(key);

        if(savedCode == null || !savedCode.equals(request.getCode())) {
            throw new RuntimeException("인증번호가 일치하지 않거나 만료되었습니다.");
        }

        redisTemplate.delete(key);
        // 인증 완료
        redisTemplate.opsForValue().set("EMAIL_VERIFIED:" + request.getPurpose().name() + ":" + request.getEmail(), "TRUE", Duration.ofMinutes(10));
    }
}
