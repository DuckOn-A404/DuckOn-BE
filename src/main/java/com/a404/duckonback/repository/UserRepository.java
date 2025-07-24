package com.a404.duckonback.repository;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    User findByUserId(String userId);
    User findByUuid(String uuid);

    User findByProviderAndProviderId(SocialProvider provider, String providerId);
}
