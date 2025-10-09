package com.a404.duckonback.service;

import com.a404.duckonback.enums.EmailPurpose;

public interface EmailVerificationService {
    void sendCode(String email, EmailPurpose purpose);
    boolean verifyCode(String email, EmailPurpose purpose, String code);
}
