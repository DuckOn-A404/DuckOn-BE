package com.a404.duckonback.service;

import com.a404.duckonback.dto.CreateRoomRequestDTO;
import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.entity.Artist;
import com.a404.duckonback.entity.Room;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LiveRoomServiceImpl implements LiveRoomService {
    private final RedisService redisService;
    private final UserService userService;
    private final RoomService roomService;
    private final ArtistService artistService;
//    private final S3Uploader s3Uploader;

    public LiveRoomDTO createRoom(CreateRoomRequestDTO req) {
        String imgUrl = "";

        User user = userService.findByUserId(req.getHostId());

        Artist artist = artistService.findById(req.getArtistId());

        Room roomEntity = Room.builder()
                .title(req.getTitle())
                .imgUrl(imgUrl)
                .createdAt(LocalDateTime.now())
                .creator(user)
                .artist(artist)
                .build();

        roomEntity = roomService.createRoom(roomEntity);
        Long roomId = roomEntity.getRoomId(); // JPA가 생성한 DB id

        LiveRoomDTO room = LiveRoomDTO.builder()
                .roomId(roomId)
                .title(req.getTitle())
                .hostId(req.getHostId())
                .imgUrl(imgUrl)
                .playlist(Collections.emptyList())
                .currentVideoIndex(0)
                .currentTime(0.0)
                .isPlaying(false)
                .lastUpdated(System.currentTimeMillis())
                .build();

        redisService.addRoomToArtist(req.getArtistId().toString(), roomId.toString());
        redisService.saveRoomInfo(roomId.toString(), room);

        return room;
    }


    public LiveRoomDTO getRoom(String roomId) {
        return redisService.getRoomInfo(roomId);
    }
}
