package com.a404.duckonback.service;

import com.a404.duckonback.entity.Follow;

import java.util.List;
import java.util.Optional;

public interface FollowService {
    Follow createFollow(Follow follow);
    Optional<Follow> getFollow(Long followerId, Long followingId);
    List<Follow> getFollowings(Long followerId);
    List<Follow> getFollowers(Long followingId);
    void deleteFollow(Long followerId, Long followingId);
    boolean isFollowing(Long followerId, Long followingId);
}
