package com.a404.duckonback.controller;

import com.a404.duckonback.dto.UserDetailInfoResponseDTO;
import com.a404.duckonback.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getMyInfo(@RequestHeader("Authorization") String authorization) {
        UserDetailInfoResponseDTO userInfo = userService.getUserDetailInfo(authorization);
        return ResponseEntity.ok(userInfo);
    }
}
