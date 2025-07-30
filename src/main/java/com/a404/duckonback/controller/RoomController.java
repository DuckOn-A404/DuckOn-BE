package com.a404.duckonback.controller;

import com.a404.duckonback.dto.CreateRoomRequestDTO;
import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.service.LiveRoomService;
import com.a404.duckonback.service.RedisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "방 관리", description = "방 생성, 조회, 삭제 등의 기능을 제공합니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final LiveRoomService liveRoomService;
    private final RedisService redisService;

    @Operation(summary = "방 생성",
            description = "새로운 라이브 방송 방을 생성합니다. 프로필 사진과 배경 이미지를 포함할 수 있습니다.")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LiveRoomDTO> createRoom(@ModelAttribute CreateRoomRequestDTO req) {
        LiveRoomDTO room = liveRoomService.createRoom(req);
        return ResponseEntity.ok(room);
    }

    @Operation(summary = "방 조회",
            description = "특정 방의 정보를 조회합니다. 방 ID를 통해 방 정보를 가져옵니다.")
    @GetMapping("/{roomId}")
    public ResponseEntity<LiveRoomDTO> getRoom(@PathVariable String roomId) {
        LiveRoomDTO room = liveRoomService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }

    @Operation(summary = "방 삭제",
            description = "특정 방을 삭제합니다. 방 ID와 아티스트 ID를 통해 방 정보를 삭제합니다.")
    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId, @RequestParam Long artistId) {
        redisService.deleteRoomInfo(artistId, roomId);
        return ResponseEntity.ok("방이 삭제되었습니다.");
    }
}
