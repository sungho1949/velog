package velog.velog.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import velog.velog.domain.user.entity.User;

import java.util.Optional;

public interface UserRepositroy extends JpaRepository<User, Long> {

    // 이메일 중복 확인
    boolean existsByEmail(String email);

    // 로그인 및 비밀번호 재설정
    Optional<User> findByEmail(String email);
}
