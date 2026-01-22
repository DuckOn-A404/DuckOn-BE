package com.a404.duckonback.service;

import java.time.Instant;

public interface AuthAuditService {
    Instant markLoggedIn(Long id);
}
