package com.a404.duckonback.controller;

import com.a404.duckonback.oauth.principal.CustomUserPrincipal;
import com.a404.duckonback.service.UserBlockService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockService userBlockService;

    /** 타 이용자 차단 */
    @PostMapping("/block/{userId}")
    public ResponseEntity<?> blockUser(
            @PathVariable String userId,  // 차단 대상자의 userId(String)
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        Long blockerId = principal.getUser().getId();  // 로그인한 사용자의 PK(Long)
        userBlockService.blockUser(blockerId, userId);
        return ResponseEntity.ok(Map.of("message", "사용자를 차단하였습니다."));
    }
}
