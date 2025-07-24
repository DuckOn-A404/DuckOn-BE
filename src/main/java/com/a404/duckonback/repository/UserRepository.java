package com.a404.duckonback.repository;

import com.a404.duckonback.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    User findByUserId(String userId);
    User findByUuid(String uuid);
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);

}
