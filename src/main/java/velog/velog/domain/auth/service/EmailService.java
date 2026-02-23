package velog.velog.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    @Async
    public void sendVerificationCode(String email) {
        // 인증 번호 랜덤으로 생성 10000~999999
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));

        // Redis에 저장(key: email, value: code, 5분)
        redisTemplate.opsForValue().set("AUTH: " + email, code, Duration.ofMinutes(5));

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Velog] 회원가입 인증번호");
        message.setText("인증번호 6자리: " + code);
        mailSender.send(message);
    }

    public boolean verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get("AUTH: " + email);
        return code.equals(savedCode);
    }
}
