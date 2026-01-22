package com.a404.duckonback.service;

import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AuthAuditServiceImpl implements AuthAuditService{

    private final UserRepository userRepository;

    @Transactional
    public Instant markLoggedIn(Long id) {
        Instant now = Instant.now();
        userRepository.updateLastLoginAt(id, now);
        return now;
    }
}
