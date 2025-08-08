package com.a404.duckonback.filter;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = null;

        // 1) 먼저 userId로 조회
        user = userRepository.findByUserIdAndDeletedFalse(username);
        // 2) userId 조회가 안 됐으면 이메일로 시도
        if (user == null) {
            user = userRepository.findByEmailAndDeletedFalse(username);
        }

        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        // OAuth2User + UserDetails 를 구현한 CustomUserPrincipal 사용
        return new CustomUserPrincipal(user);
    }
}