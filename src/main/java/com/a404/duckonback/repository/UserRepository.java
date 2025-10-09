package com.a404.duckonback.repository;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.SocialProvider;
import com.a404.duckonback.repository.projection.UserBrief;
import org.springframework.data.domain.Page;
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
public interface UserRepository extends JpaRepository<User, Long> {
    User findByIdAndDeletedFalse(Long id);
    User findByEmailAndDeletedFalse(String email);
    User findByUserIdAndDeletedFalse(String userId);

    boolean existsByEmailAndDeletedFalse(String email);
    boolean existsByUserIdAndDeletedFalse(String userId);
    boolean existsByNicknameAndDeletedFalse(String nickname);

    // 소셜 로그인용: deleted=false 버전 추가 (기존 메서드는 점진적 교체)
    Optional<User> findByProviderAndProviderIdAndDeletedFalse(SocialProvider provider, String providerId);

    // 구버전은 실수 방지용으로 deprecate
    @Deprecated
    User findByProviderAndProviderId(SocialProvider provider, String providerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.penalties WHERE u.email = :email AND u.deleted = false")
    Optional<User> findByEmailWithPenalties(@Param("email") String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.penalties WHERE u.userId = :userId AND u.deleted = false")
    Optional<User> findByUserIdWithPenalties(@Param("userId") String userId);

    @Query("""
    SELECT u 
    FROM User u
      LEFT JOIN FETCH u.subjectFollows sf
      LEFT JOIN FETCH sf.subject
    WHERE u.userId = :userId AND u.deleted = false
    """)
    Optional<User> findUserDetailWithSubjectFollows(@Param("userId") String userId);

    /** 같은 아티스트 팔로워 (탈퇴 제외) */
    @Query("""
      SELECT u.userId AS userId, u.nickname AS nickname, u.imgUrl AS imgUrl
      FROM SubjectFollow sf JOIN sf.user u
      WHERE sf.subject.id = :subjectId
        AND u.deleted = false
      ORDER BY sf.createdAt DESC
    """)
    List<UserBrief> findArtistFollowersBrief(@Param("subjectId") Long subjectId, Pageable pageable);

    /** 같은 아티스트에서 최근 방 호스트 (탈퇴 제외) */
    @Query("""
      SELECT
        u.userId   AS userId,
        u.nickname AS nickname,
        u.imgUrl   AS imgUrl,
        MAX(r.createdAt) AS lastCreated
      FROM Room r
        JOIN r.creator u
      WHERE r.subject.id = :subjectId
        AND r.createdAt >= :since
        AND u.deleted = false
      GROUP BY u.userId, u.nickname, u.imgUrl
      ORDER BY lastCreated DESC
    """)
    List<UserBrief> findRecentHostsBrief(@Param("subjectId") Long subjectId,
                                         @Param("since") LocalDateTime since,
                                         Pageable pageable);

    /** 얇은 프로필 벌크 조회 (탈퇴 제외) */
    @Query("""
      SELECT u.userId AS userId, u.nickname AS nickname, u.imgUrl AS imgUrl
      FROM User u
      WHERE u.userId IN :userIds
        AND u.deleted = false
    """)
    List<UserBrief> findBriefsByUserIdIn(@Param("userIds") Collection<String> userIds);

    /** 랜덤 보충용: 활성 유저 페이징 */
    Page<User> findAllByDeletedFalse(Pageable pageable);

}
