package com.a404.duckonback.domain.user.repository;

import com.a404.duckonback.domain.user.entity.Follow;
import com.a404.duckonback.domain.user.entity.FollowId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    List<Follow> findByFollower_Id(Long followerId);
    List<Follow> findByFollowing_Id(Long followingId);
    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);
    void deleteByFollower_IdAndFollowing_Id(Long followerId, Long followingId);
    void deleteByFollower_UserIdAndFollowing_UserId(String followerUserId, String followingUserId);
    boolean existsByFollower_UserIdAndFollowing_UserId(String followerUserId, String followingUserId);
}
