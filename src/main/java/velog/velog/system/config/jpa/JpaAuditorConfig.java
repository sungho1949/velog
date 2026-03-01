package velog.velog.system.config.jpa;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaAuditorConfig {
    // AuditorAwareImpl을 찾아 사용하기 위한 Config
}
