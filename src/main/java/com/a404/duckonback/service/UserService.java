package com.a404.duckonback.service;

import com.a404.duckonback.entity.User;

public interface UserService {
    User findByEmail(String email);
    User findByUserId(String userId);
    void save(User user);
}
