package velog.velog.common.auditor;

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import velog.velog.system.security.model.UserPrincipal;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    @NonNull
    public Optional<Long> getCurrentAuditor() {
        // 1. SecurityContext로 인증 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 인증 정보 X, 익명일 경우
        if(authentication == null || !authentication.isAuthenticated() || authentication.getName().equals("anonymousUser")) {
            return Optional.empty();
        }

        // 3. Principal 확인 및 ID 반환
        Object principal = authentication.getPrincipal();
        if(principal instanceof UserPrincipal userPrincipal) {
            return Optional.ofNullable(userPrincipal.getUserId());
        }
        return Optional.empty();
    }
}
