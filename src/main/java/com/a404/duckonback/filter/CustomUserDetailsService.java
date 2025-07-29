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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // username으로 User 조회 (userId 혹은 email 기반)
        User user = userRepository.findByUserId(username);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
        }
        // CustomUserDetails는 UserDetails 구현체
        return new CustomUserDetails(user);
    }
}
