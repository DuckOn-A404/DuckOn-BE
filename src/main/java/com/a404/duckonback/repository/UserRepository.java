package com.a404.duckonback.repository;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    User findByEmail(String email);
    User findByUserId(String userId);
    User findById(Long id);
    boolean existsByEmail(String email);
    boolean existsByUserId(String userId);
    boolean existsByNickname(String nickname);
    void deleteByUserId(String userId);

    User findByProviderAndProviderId(SocialProvider provider, String providerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.penalties WHERE u.email = :email")
    Optional<User> findByEmailWithPenalties(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.penalties WHERE u.userId = :userId")
    Optional<User> findByUserIdWithPenalties(@Param("userId") String userId);

    @Query("""
SELECT u FROM User u
LEFT JOIN FETCH u.artistFollows af
LEFT JOIN FETCH af.artist
WHERE u.userId = :userId
""")
    Optional<User> findUserDetailWithArtistFollows(@Param("userId") String userId);


}
