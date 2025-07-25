package com.a404.duckonback.service;

import com.a404.duckonback.dto.RoomDTO;
import com.a404.duckonback.dto.UserDetailInfoResponseDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.RoomRepository;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RoomRepository roomRepository;
    private final JWTUtil jWTUtil;

    public User findByEmail(String email) {
        return userRepository.findByEmailWithPenalties(email)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED));
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserIdWithPenalties(userId)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED));
    }

    public void save(User user) { userRepository.save(user);}


    public boolean isEmailDuplicate(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean isUserIdDuplicate(String userId) {
        return userRepository.existsByUserId(userId);
    }

    public boolean isNicknameDuplicate(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public UserDetailInfoResponseDTO getUserDetailInfo(String authorization) {
        String accessToken = jwtUtil.extractAndValidateToken(authorization);

        String userId = jwtUtil.getClaims(accessToken).getSubject();
        User user = userRepository.findByUserId(userId);

        if(user == null){
            throw new CustomException("사용자를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED);
        }

//        List<Room> rooms = roomRepository.findByUserId(userId);
        List<RoomDTO> rooms = null;


        return UserDetailInfoResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole().toString())
                .language(user.getLanguage())
                .imgUrl(user.getImgUrl())
                .artistList(user.getArtistFollows().stream().map(
                        artistFollow -> artistFollow.getArtist().getArtistId()
                ).toList())
                .followingCount(user.getFollowing().size())
                .followerCount(user.getFollowers().size())
                .bannedTill(null) // join
                .password(user.getPassword())
                .roomList(rooms) // 개발 필요
                .build();
    }

    @Transactional
    public void deleteUser(String authorization, String refreshToken){
        String accessToken = jwtUtil.extractAndValidateToken(authorization);

        String userId = jwtUtil.getClaims(accessToken).getSubject();
        userRepository.deleteByUserId(userId);

        //refreshToken 블랙리스트 등록
    }


}
