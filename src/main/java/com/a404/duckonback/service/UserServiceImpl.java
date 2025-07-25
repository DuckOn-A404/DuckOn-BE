package com.a404.duckonback.service;

import com.a404.duckonback.dto.PenaltyDTO;
import com.a404.duckonback.dto.RoomDTO;
import com.a404.duckonback.dto.UserDetailInfoResponseDTO;
import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.entity.Room;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.RoomRepository;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.util.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RoomRepository roomRepository;
    private final PenaltyService penaltyService;

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

    public UserDetailInfoResponseDTO getUserDetailInfo(String authorization) {
        String accessToken = jwtUtil.extractAndValidateToken(authorization);

        String userId = jwtUtil.getClaims(accessToken).getSubject();
        User user = userRepository.findByUserId(userId);

        if(user == null){
            throw new CustomException("사용자를 찾을 수 없습니다", HttpStatus.UNAUTHORIZED);
        }

        List<Room> rooms = roomRepository.findByCreator_Id(user.getId());
        List<RoomDTO> roomList = (rooms != null ? rooms : List.<Room>of()).stream()
                .map(room -> RoomDTO.builder()
                        .roomId(room.getRoomId())
                        .title(room.getTitle())
                        .imgUrl(room.getImgUrl())
                        .createdAt(room.getCreatedAt())
                        .creatorId(room.getCreator().getUserId())
                        .artistId(room.getArtist().getArtistId())
                        .build())
                .toList();

        List<Penalty> penalties = penaltyService.getActivePenaltiesByUser(user.getId());
        List<PenaltyDTO> pennaltyList = penalties.stream()
                .map(penalty -> PenaltyDTO.builder()
                        .startAt(penalty.getStartAt())
                        .endAt(penalty.getEndAt())
                        .reason(penalty.getReason())
                        .penaltyType(penalty.getPenaltyType().toString())
                        .status(penalty.getStatus().toString())
                        .build())
                .toList();

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
                .password(user.getPassword())
                .roomList(roomList)
                .penaltyList(pennaltyList)
                .build();
    }

}
