package com.a404.duckonback.service;

import com.a404.duckonback.dto.RoomDTO;
import com.a404.duckonback.dto.SignupRequestDTO;
import com.a404.duckonback.dto.UserInfoResponseDTO;
import com.a404.duckonback.entity.Room;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.UserRole;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.RoomRepository;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RoomRepository roomRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId);
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

    public UserInfoResponseDTO getUserInfo(String accessToken) {
        String userId = jwtUtil.getClaims(accessToken).getSubject();

        User user = userRepository.findByUserId(userId);

        if(user == null){
            throw new CustomException("사용자를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED);
        }

//        List<Room> rooms = roomRepository.findByUserId(userId);
        List<RoomDTO> rooms = null;


        return UserInfoResponseDTO.builder()
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

}
