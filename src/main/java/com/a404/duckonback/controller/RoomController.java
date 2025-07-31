package com.a404.duckonback.controller;

import com.a404.duckonback.dto.CreateRoomRequestDTO;
import com.a404.duckonback.dto.LiveRoomDTO;
import com.a404.duckonback.service.LiveRoomService;
import com.a404.duckonback.service.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rooms")
public class RoomController {

    private final LiveRoomService liveRoomService;
    private final RedisService redisService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LiveRoomDTO> createRoom(@ModelAttribute CreateRoomRequestDTO req) {
        LiveRoomDTO room = liveRoomService.createRoom(req);
        return ResponseEntity.ok(room);
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<LiveRoomDTO> getRoom(@PathVariable Long roomId) {
        LiveRoomDTO room = liveRoomService.getRoom(roomId);
        return ResponseEntity.ok(room);
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId, @RequestParam Long artistId) {
        redisService.deleteRoomInfo(artistId, roomId);
        return ResponseEntity.ok("방이 삭제되었습니다.");
    }
}
