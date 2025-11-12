package com.a404.duckonback.controller;

import com.a404.duckonback.dto.*;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.RoomSyncEventType;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.filter.CustomUserPrincipal;
import com.a404.duckonback.response.ErrorCode;
import com.a404.duckonback.service.ArtistService;
import com.a404.duckonback.service.LiveRoomService;
import com.a404.duckonback.service.RedisService;
import com.a404.duckonback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Tag(name = "방 관리", description = "방 생성, 조회, 삭제 등의 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final LiveRoomService liveRoomService;
    private final RedisService redisService;
    private final ArtistService artistService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;

    @Operation(summary = "방 생성",
            description = "새로운 라이브 방송 방을 생성합니다. 프로필 사진과 배경 이미지를 포함할 수 있습니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LiveRoomDTO> createRoom(
            @AuthenticationPrincipal CustomUserPrincipal principal,
            @ModelAttribute CreateRoomRequestDTO req)
    {
        if (principal == null) {
            throw new CustomException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        req.setHostId(principal.getUser().getUserId());

        LiveRoomDTO room = liveRoomService.createRoom(req);
        return ResponseEntity.ok(room);
    }

//    @Operation(summary = "방 조회",
//            description = "특정 방의 정보를 조회합니다. 방 ID를 통해 방 정보를 가져옵니다.")
//    @GetMapping("/{roomId}")
//    public ResponseEntity<LiveRoomDTO> getRoom(@PathVariable Long roomId) {
//        LiveRoomDTO room = liveRoomService.getRoom(roomId);
//        return ResponseEntity.ok(room);
//    }

    @Operation(summary = "방 삭제",
            description = "특정 방을 삭제합니다. 방 ID와 아티스트 ID를 통해 방 정보를 삭제합니다.")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId,
                                        @RequestParam Long artistId,
                                        @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            throw new CustomException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        LiveRoomDTO room = redisService.getRoomInfo(roomId.toString()); // 없으면 내부에서 404 throw

        if (!principal.getUser().getUserId().equals(room.getHostId())) {
            throw new CustomException("호스트만 방을 삭제할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        redisService.deleteRoomInfo(artistId, roomId);

        LiveRoomSyncDTO dto = LiveRoomSyncDTO.builder()
                .eventType(RoomSyncEventType.ROOM_DELETED)
                .roomId(roomId)
                .title(null)
                .hostId(null)
                .hostNickname(null)
                .playlist(java.util.Collections.emptyList())
                .currentVideoIndex(0)
                .currentTime(0.0)
                .playing(false)
                .lastUpdated(System.currentTimeMillis())
                .build();

        messagingTemplate.convertAndSend("/topic/room/" + roomId, dto);

        return ResponseEntity.ok("방이 삭제되었습니다.");
    }

    @Operation(summary = "방 제목 변경",
            description = "특정 방의 제목을 변경합니다. 방 ID와 새로운 제목을 받아서 방 정보를 갱신합니다.")
    @PatchMapping("/{roomId}/title")
    public ResponseEntity<?> updateRoomTitle(@PathVariable Long roomId,
                                             @RequestParam String title,
                                             @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (principal == null) {
            throw new CustomException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        // Redis에서 현재 방 정보 조회 (없으면 내부에서 404 throw)
        LiveRoomDTO room = redisService.getRoomInfo(roomId.toString());

        // 호스트만 변경 가능
        if (!principal.getUser().getUserId().equals(room.getHostId())) {
            throw new CustomException("호스트만 방 정보를 변경할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        // Redis에 저장된 방 정보 갱신
        room.setTitle(title);

        // 변경 이벤트 브로드캐스트
        LiveRoomSyncDTO dto = LiveRoomSyncDTO.builder()
                .eventType(RoomSyncEventType.ROOM_UPDATE)
                .roomId(roomId)
                .title(title)
                .hostId(room.getHostId())
                .hostNickname(room.getHostNickname())
                .playlist(room.getPlaylist())
                .currentVideoIndex(room.getCurrentVideoIndex())
                .currentTime(room.getCurrentTime())
                .playing(room.isPlaying())
                .lastUpdated(System.currentTimeMillis())
                .build();

        redisService.updateRoomInfo(dto);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, dto);

        return ResponseEntity.ok("방 제목이 변경되었습니다.");
    }

    // RoomController.java (일부 추가)
    @Operation(summary = "플레이리스트 갱신(전체 교체/삭제/순서변경)",
            description = "전체 플레이리스트와 다음 재생 인덱스를 받아 방 상태를 갱신하고 브로드캐스트합니다.")
    @PostMapping("/{roomId}/playlist")
    public ResponseEntity<?> updatePlaylist(
            @PathVariable Long roomId,
            @RequestBody PlaylistUpdateRequestDTO req,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        if (principal == null) {
            throw new CustomException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        // 현재 방 상태 조회 (없으면 내부에서 404 throw)
        LiveRoomDTO room = redisService.getRoomInfo(roomId.toString());

        // 호스트만 변경 가능
        if (!principal.getUser().getUserId().equals(room.getHostId())) {
            throw new CustomException("호스트만 변경할 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        if (req.getPlaylist() == null || req.getPlaylist().isEmpty()) {
            throw new CustomException("플레이리스트는 비어있을 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 입력 정리/검증
        java.util.List<String> playlist = req.getPlaylist().stream()
        .filter(java.util.Objects::nonNull)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();

        int size = playlist.size();
        int nextIndex = (req.getNextIndex() == null) ? 0 : req.getNextIndex();

        if (size == 0) {
            nextIndex = 0; // 비었으면 항상 0
        } else {
            if (nextIndex < 0) nextIndex = 0;
            if (nextIndex >= size) nextIndex = size - 1;
        }

        // 재생/시간 정책:
        // - playlist가 비면 playing=false, time=0
        // - 비지 않으면, 기존 playing 유지 (원하면 '항상 true'로 바꿔도 됨)
        boolean playing = size > 0 && room.isPlaying();
        double currentTime = 0.0; // 순서변경/삭제 즉시 해당 곡 처음부터 재생

        long now = System.currentTimeMillis();

        LiveRoomSyncDTO dto = LiveRoomSyncDTO.builder()
                .eventType(RoomSyncEventType.ROOM_UPDATE)
                .roomId(roomId)
                .hostId(room.getHostId())
                .hostNickname(room.getHostNickname())
                .title(room.getTitle())
                .playlist(playlist)
                .currentVideoIndex(nextIndex)
                .currentTime(currentTime)
                .playing(playing)
                .lastUpdated(now)
                .build();

        // Redis 갱신
        redisService.updateRoomInfo(dto);

        // 브로드캐스트
        messagingTemplate.convertAndSend("/topic/room/" + roomId, dto);

        return ResponseEntity.ok(dto);
    }



    @Operation(summary ="방 입장",
            description = "특정 방을 입장합니다. 로그인한 유저, 로그인하지 않은 유저 모두 입장 가능합니다.\n"
                            + "잠겨있는 경우 에러 반환(입장질문 포함)하며 정답을 포함하여 재요청을 수행하면 됩니다.")
    @PostMapping("/{roomId}/enter")
    public ResponseEntity<Map<String,Object>> enterRoom(
//    public ResponseEntity<LiveRoomDTO> enterRoom(
            @PathVariable Long roomId,
            @RequestBody(required = false) EntryAnswerRequestDTO request,
            @AuthenticationPrincipal CustomUserPrincipal principal,
            HttpServletRequest http
    ) {
        LiveRoomDTO room = redisService.getRoomInfo(roomId.toString());
        String entryAnswer = (request != null) ? request.getEntryAnswer() : null;

        if (room == null) {
            throw new CustomException("존재하지 않는 방입니다", HttpStatus.NOT_FOUND);
        }

        if (room.isLocked()) {

            if (entryAnswer == null || entryAnswer.isBlank()) {
                Map<String, Object> map = new HashMap<>();
                map.put("entryQuestion", room.getEntryQuestion());
                map.put("hostId", room.getHostId());
                throw new CustomException(
                        "잠금 방입니다. 입장 질문에 대한 정답을 입력해야 합니다.",
                        HttpStatus.FORBIDDEN,   // 정답 미입력: 403 (FORBIDDEN)
                        map
                );
            }

            if (!entryAnswer.equals(room.getEntryAnswer())) {
                throw new CustomException("정답이 일치하지 않습니다.", HttpStatus.FORBIDDEN);  // 정답 오답: 403 (FORBIDDEN)
            }
        }

        Map<String, Object> result = new HashMap<>();

        // 로그인 사용자인 경우 참여자 목록에 추가
        if (principal != null) {
            String userId = principal.getUser().getUserId();

            if (redisService.isUserBanned(roomId.toString(), userId)) {
                throw new CustomException("강퇴된 사용자입니다. 입장할 수 없습니다.",ErrorCode.ROOM_BANNED_USER);
            }

            redisService.addUserToRoom(roomId.toString(), principal.getUser());
            result.put("userId", userId);
            result.put("nickname", principal.getUser().getNickname());
        }else{// 로그인 하지 않더라도 참여자 수 증가
            String sessionId = http.getSession(true).getId();
            String guestId = "guest:" + sessionId;
            String nickname = "익명의 오리#" + java.util.UUID.randomUUID().toString().substring(0, 6);;
            result.put("userId", guestId);
            result.put("nickname", nickname);
            redisService.addParticipantCountToRoom(roomId.toString());
        }

        long participantCount = redisService.getRoomUserCount(roomId.toString());
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/presence",
                new RoomPresenceDTO(roomId, participantCount)
        );
        room.setParticipantCount(participantCount);

        result.put("room", room);

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "방 퇴장", description = "현재 로그인한 사용자가 방에서 퇴장합니다.")
    @PostMapping("/{roomId}/exit")
    public ResponseEntity<?> exitRoom(
            @PathVariable Long roomId,
            @RequestParam Long artistId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        if (principal != null) {
            redisService.removeUserFromRoom(
                    artistId.toString(),
                    roomId.toString(),
                    principal.getUser()
            );
        }else{
            redisService.decreaseParticipantCountFromRoom(roomId.toString());
        }

        long participantCount = redisService.getRoomUserCount(roomId.toString());
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/presence",
                new RoomPresenceDTO(roomId, participantCount)
        );

        return ResponseEntity.ok("방에서 퇴장하였습니다.");
    }

    @Operation(summary = "방 강퇴", description = "방장이 현재 접속한 사용자를 방에서 강제 퇴장합니다.")
    @PostMapping("/{roomId}/eject/{nickname}")
    public ResponseEntity<?> ejectUserFromRoom(
            @PathVariable Long roomId,
            @PathVariable String nickname,
            @RequestParam Long artistId,
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {

        if (principal == null) {
            throw new CustomException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);
        }

        // Redis에서 현재 방 정보 조회 (없으면 내부에서 404 throw)
        LiveRoomDTO room = redisService.getRoomInfo(roomId.toString());

        // 호스트만 강제퇴장 가능
        if (!principal.getUser().getUserId().equals(room.getHostId())) {
            throw new CustomException("호스트만 강제퇴장 시킬 수 있습니다.", HttpStatus.FORBIDDEN);
        }

        User target = userService.findByNickname(nickname);
        
        
        //강퇴 대상 user에게 강퇴 메세지 전송
        messagingTemplate.convertAndSendToUser(target.getUserId(),"/queue/kick",roomId);

        redisService.removeUserFromRoom(
                artistId.toString(),
                roomId.toString(),
                target
        );

        long participantCount = redisService.getRoomUserCount(roomId.toString());
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId + "/presence",
                new RoomPresenceDTO(roomId, participantCount)
        );

        return ResponseEntity.ok("방에서 강퇴하였습니다.");
    }

    @Operation(summary = "방 목록 조회", description = "현재 존재하는 모든 방 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoomSummaries(@RequestParam Long artistId) {
        List<LiveRoomSummaryDTO> rooms = redisService.getAllRoomSummaries(artistId);

        Map<String, Object> response = new HashMap<>();
        response.put("roomInfoList", rooms);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "홈 화면 아티스트별 라이브룸 조회", 
           description = "홈 화면에 표시할 랜덤 아티스트와 각 아티스트의 인기 라이브룸을 조회합니다. (참여자 수 내림차순)")
    @GetMapping("/home")
    public ResponseEntity<Map<String, Object>> getHomeRooms(
            @RequestParam(defaultValue = "3") int artistCount,
            @RequestParam(defaultValue = "6") int roomsPerArtist) {
        
        // 기존 ArtistService를 활용하여 랜덤 아티스트 조회
        List<ArtistDTO> randomArtists = artistService.getRandomArtists(artistCount);
        
        List<Long> artistIds = randomArtists.stream()
                .map(ArtistDTO::getArtistId)
                .toList();
        
        // 각 아티스트별 라이브룸 조회 (참여자 수 내림차순)
        List<HomeArtistRoomDTO> artistRooms = redisService.getHomeArtistRooms(artistIds, roomsPerArtist);
        
        Map<String, Object> response = new HashMap<>();
        response.put("artistRooms", artistRooms);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "트렌딩 방 조회(페이징)",
            description = "참여자(시청자) 수가 많은 순으로 트렌딩 방을 페이지 단위로 조회합니다."
    )
    @GetMapping("/trending")
    public ResponseEntity<Map<String, Object>> getTrendingRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (page < 1 || size < 1) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "page/size는 1 이상의 정수여야 합니다."));
        }

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<RoomListInfoDTO> dtoPage = redisService.getTrendingRooms(pageable);

        return ResponseEntity.ok(Map.of(
                "roomInfoList", dtoPage.getContent(),
                "page", page,
                "size", size,
                "totalPages", dtoPage.getTotalPages(),
                "totalElements", dtoPage.getTotalElements(),
                "hasNext", dtoPage.hasNext()
        ));
    }


}
