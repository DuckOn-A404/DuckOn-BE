package com.a404.duckonback.common.security;

import com.a404.duckonback.common.filter.CustomUserPrincipal;
import com.a404.duckonback.domain.user.entity.User;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if(auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();

        // 1. CustomUserPrincipal에서 userId 추출
        if(principal instanceof CustomUserPrincipal p){
            return Optional.ofNullable(p.getId());
        }

        // 2. @AuthenticationPrincipal User principal 로 넣는 경우 있어서 방어
        if(principal instanceof User u){
            return Optional.ofNullable(u.getId());
        }

        // 3. 그 외 문자열(anonymousUser 등)
        return Optional.empty();
    }
}
