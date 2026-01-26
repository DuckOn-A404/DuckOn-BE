package com.a404.duckonback.common.filter;

import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.domain.user.repository.UserRepository;
import com.a404.duckonback.domain.penalty.service.PenaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PenaltyService penaltyService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserIdAndDeletedFalse(username);
        if(user == null){
            user = userRepository.findByEmailAndDeletedFalse(username);
        }
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }

        // 소셜 계정은 비밀번호 로그인 차단
        if(Boolean.FALSE.equals(user.getHasLocalCredential())){
            throw new org.springframework.security.authentication.BadCredentialsException("소셜 계정은 비밀번호 로그인을 사용할 수 없습니다.");
        }

        if(penaltyService.isAccountSuspended(user.getId())){
            throw new org.springframework.security.authentication.DisabledException("계정이 정지되었습니다.");
        }

        // OAuth2User + UserDetails 를 구현한 CustomUserPrincipal 사용
        return new CustomUserPrincipal(user);
    }
}