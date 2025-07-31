package com.a404.duckonback.service;

import com.a404.duckonback.entity.Follow;
import com.a404.duckonback.entity.FollowId;
import com.a404.duckonback.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;

    @Override
    public Follow createFollow(Follow follow) {
        return followRepository.save(follow);
    }

    @Override
    public Optional<Follow> getFollow(Long followerId, Long followingId) {
        return followRepository.findById(new FollowId(followerId, followingId));
    }

    @Override
    public List<Follow> getFollowings(Long followerId) {
        return followRepository.findByFollower_Id(followerId);
    }

    @Override
    public List<Follow> getFollowers(Long followingId) {
        return followRepository.findByFollowing_Id(followingId);
    }

    @Override
    public void deleteFollow(Long followerId, Long followingId) {
        followRepository.deleteByFollower_IdAndFollowing_Id(followerId, followingId);
    }

    @Override
    public void deleteFollow(String followerUserId, String followingUserId) {
        followRepository.deleteByFollower_UserIdAndFollowing_UserId(followerUserId, followingUserId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId);
    }

    @Override
    public boolean isFollowing(String followerUserId, String followingUserId) {
        return followRepository.existsByFollower_UserIdAndFollowing_UserId(followerUserId, followingUserId);
    }
}
