package com.a404.duckonback.service;

import com.a404.duckonback.dto.CreateRoomRequestDTO;
import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.dto.LiveRoomSummaryDTO;
import com.a404.duckonback.entity.Artist;
import com.a404.duckonback.entity.Room;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class LiveRoomServiceImpl implements LiveRoomService {
    private final RedisService redisService;
    private final UserService userService;
    private final RoomService roomService;
    private final ArtistService artistService;
    private final S3Service s3Service;
    private final StringRedisTemplate stringRedisTemplate;

    private final @Qualifier("roomTemplate")
    RedisTemplate<String, LiveRoomDTO> roomTemplate;

    private static final String ROOM_KEY_PREFIX = "room:";


    public LiveRoomDTO createRoom(CreateRoomRequestDTO req) {
        
        //락 설정
        if (!redisService.acquireCreateRoomLock(req.getHostId())) {
            // 이미 같은 호스트가 방 생성 중인 상태
            throw new CustomException(ErrorCode.ROOM_CREATION_CONFLICT);
        }

        try{
            if(hasActiveRoomByHost(req.getHostId())) {
                throw new CustomException(ErrorCode.TOO_MANY_ROOMS);
            }

            MultipartFile file = req.getThumbnailImg();
            String imgUrl = null;
            if (file != null && !file.isEmpty()) {
                imgUrl = s3Service.uploadFile(file);
            }

            User user = userService.findByUserId(req.getHostId());

            Artist artist = artistService.findById(req.getArtistId());

            Room roomEntity = Room.builder()
                    .title(req.getTitle())
                    .imgUrl(imgUrl)
                    .createdAt(LocalDateTime.now())
                    .creator(user)
                    .artist(artist)
                    .build();

            if (req.isLocked()) {
                if (isNullOrBlank(req.getEntryQuestion()) || isNullOrBlank(req.getEntryAnswer())) {
                    throw new CustomException("잠금 방을 생성할 때는 입장 질문과 정답을 모두 입력해야 합니다.", HttpStatus.BAD_REQUEST);
                }
            }

            roomEntity = roomService.createRoom(roomEntity);
            Long roomId = roomEntity.getRoomId(); // JPA가 생성한 DB id

            List<String> playList = new ArrayList<>();
            playList.add(req.getVideoId());

            LiveRoomDTO room = LiveRoomDTO.builder()
                    .roomId(roomId)
                    .title(req.getTitle())
                    .artistId(req.getArtistId())
                    .hostId(req.getHostId())
                    .hostNickname(req.getHostNickname())
                    .imgUrl(imgUrl)
                    .playlist(playList)
                    .currentVideoIndex(0)
                    .currentTime(0.0)
                    .playing(false)
                    .lastUpdated(System.currentTimeMillis())
                    .locked(req.isLocked())
                    .entryQuestion(req.getEntryQuestion())
                    .entryAnswer(req.getEntryAnswer())
                    .build();

            redisService.addRoomToArtist(req.getArtistId().toString(), roomId.toString());
            redisService.saveRoomInfo(roomId.toString(), room);

            return room;
        } finally {
            //락 해제
            redisService.releaseCreateRoomLock(req.getHostId());
        }


    }

    @Override
    public boolean hasActiveRoomByHost(String hostUserId) {
        // KEYS 사용 (서비스 규모 커지면 SCAN으로 교체 가능)
        Set<String> keys = redisService.scanKeys(ROOM_KEY_PREFIX + "*");
//        Set<String> keys = stringRedisTemplate.keys(ROOM_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) return false;

        for (String key : keys) {
            int nextColon = key.indexOf(':', ROOM_KEY_PREFIX.length());
            if (nextColon != -1) continue;

            LiveRoomDTO dto = roomTemplate.opsForValue().get(key);
            if (dto == null) continue;

            if (hostUserId.equals(dto.getHostId())) {
                return true;
            }
        }
        return false;
    }


    public LiveRoomDTO getRoom(Long roomId) {
        return redisService.getRoomInfo(roomId.toString());
    }

    private boolean isNullOrBlank(String s) {
        return s == null || s.trim().isEmpty();
    }


}