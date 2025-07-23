package com.a404.duckonback.service;

import com.a404.duckonback.dto.SignupRequestDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.UserRole;
import com.a404.duckonback.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final ArtistServiceImpl artistServiceImpl;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findByUserId(String userId) {
        return userRepository.findByUserId(userId);
    }

    public ResponseEntity<?> signup(SignupRequestDTO dto){
        MultipartFile file = dto.getProfileImg();
        String imgUrl = null;
        if (file != null && !file.isEmpty()) {
            //이미지 저장
        }

        User user = User.builder()
                .uuid(UUID.randomUUID().toString())
                .email(dto.getEmail())
                .userId(dto.getUserId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .createdAt(LocalDateTime.now())
                .role(UserRole.USER)
                .language(dto.getLanguage())
                .imgUrl(imgUrl)
                .build();

        userRepository.save(user);

        if (dto.getArtistList() != null && !dto.getArtistList().isEmpty()) {
            artistServiceImpl.followArtists(user.getUuid(), dto.getArtistList());
        }

        return ResponseEntity.ok().body("회원가입이 성공적으로 완료되었습니다!");
    }
}
