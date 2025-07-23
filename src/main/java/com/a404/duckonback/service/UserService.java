package com.a404.duckonback.service;

import com.a404.duckonback.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    User createUser(User user);
    Optional<User> getUserByUuid(String uuid);
    List<User> getAllUsers();
    User updateUser(String uuid, User updatedUser);
    void deleteUser(String uuid);

}
