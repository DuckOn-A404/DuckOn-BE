package com.a404.duckonback.repository;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.SocialProvider;
import com.a404.duckonback.repository.projection.UserBrief;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
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
      SELECT u 
      FROM User u
        LEFT JOIN FETCH u.artistFollows af
        LEFT JOIN FETCH af.artist
      WHERE u.userId = :userId
    """)
    Optional<User> findUserDetailWithArtistFollows(@Param("userId") String userId);

    /** 같은 아티스트 팔로워(가볍게 프로젝션, 정렬 = 최근 팔로우 순), LIMIT Pageable */
    @Query("""
      SELECT u.userId AS userId, u.nickname AS nickname, u.imgUrl AS imgUrl
      FROM ArtistFollow af JOIN af.user u
      WHERE af.artist.artistId = :artistId
      ORDER BY af.createdAt DESC
    """)
    List<UserBrief> findArtistFollowersBrief(@Param("artistId") Long artistId, Pageable pageable);

    /** 같은 아티스트에서 최근 방 호스트(최근 생성 순), LIMIT Pageable */
    @Query("""
      SELECT
        u.userId   AS userId,
        u.nickname AS nickname,
        u.imgUrl   AS imgUrl,
        MAX(r.createdAt) AS lastCreated
      FROM Room r
        JOIN r.creator u
      WHERE r.artist.artistId = :artistId
        AND r.createdAt >= :since
      GROUP BY u.userId, u.nickname, u.imgUrl
      ORDER BY lastCreated DESC
    """)
    List<UserBrief> findRecentHostsBrief(@Param("artistId") Long artistId,
                                         @Param("since") LocalDateTime since,
                                         Pageable pageable);

    /** 결과 집합 userId로 한번에 얇게 조회 (IN) */
    @Query("""
      SELECT u.userId AS userId, u.nickname AS nickname, u.imgUrl AS imgUrl
      FROM User u
      WHERE u.userId IN :userIds
    """)
    List<UserBrief> findBriefsByUserIdIn(@Param("userIds") Collection<String> userIds);

}