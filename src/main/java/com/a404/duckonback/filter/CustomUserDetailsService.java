package com.a404.duckonback.filter;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.oauth.principal.CustomUserPrincipal;
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
        User user = userRepository.findByUserId(username);
        if (user == null)
            throw new UsernameNotFoundException(username + " 을 찾을 수 없습니다.");
        // OAuth2User + UserDetails 를 구현한 CustomUserPrincipal 사용
        return new CustomUserPrincipal(user);
    }
}