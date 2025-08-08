package com.a404.duckonback.repository;

import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.SocialProvider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

    /** 같은 아티스트를 팔로우 중인 유저 */
    @Query("""
        select distinct u
        from User u
          join u.artistFollows af
        where af.artist.artistId = :artistId
    """)
    List<User> findUsersFollowingArtist(@Param("artistId") Long artistId);

    /** 같은 아티스트에서 최근 방을 만든 유저(호스트) */
    @Query("""
        select c
        from Room r
          join r.creator c
        where r.artist.artistId = :artistId
        group by c
        order by max(r.createdAt) desc
    """)
    List<User> findRecentRoomCreatorsByArtist(@Param("artistId") Long artistId);

    /** 친구의 친구(2-hop). 내 팔로잉이 팔로우하는 사람들(나 자신 제외) */
    @Query("""
        select distinct f2.following
        from Follow f1
          join Follow f2 on f1.following.id = f2.follower.id
        where f1.follower.userId = :myUserId
          and f2.following.userId <> :myUserId
    """)
    List<User> findFriendsOfFriends(@Param("myUserId") String myUserId);

}
