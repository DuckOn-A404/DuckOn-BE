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
    User findByIdAndDeletedFalse(Long id);
    User findByEmailAndDeletedFalse(String email);
    User findByUserIdAndDeletedFalse(String userId);

    boolean existsByEmailAndDeletedFalse(String email);
    boolean existsByUserIdAndDeletedFalse(String userId);
    boolean existsByNicknameAndDeletedFalse(String nickname);

    User findByProviderAndProviderId(SocialProvider provider, String providerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.penalties WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmailWithPenalties(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.penalties WHERE u.userId = :userId AND u.deleted = false")
    Optional<User> findByUserIdWithPenalties(@Param("userId") String userId);

    @Query("""
    SELECT u 
    FROM User u
      LEFT JOIN FETCH u.artistFollows af
      LEFT JOIN FETCH af.artist
    WHERE u.userId = :userId AND u.deleted = false
    """)
    Optional<User> findUserDetailWithArtistFollows(@Param("userId") String userId);


}
