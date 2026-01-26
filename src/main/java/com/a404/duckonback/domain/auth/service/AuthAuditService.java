package com.a404.duckonback.domain.auth.service;

import java.time.Instant;

public interface AuthAuditService {
    Instant markLoggedIn(Long id);
}
