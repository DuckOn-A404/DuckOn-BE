package com.a404.duckonback.service;

import com.a404.duckonback.dto.LoginRequestDTO;
import com.a404.duckonback.dto.LoginResponseDTO;
import com.a404.duckonback.dto.SignupRequestDTO;
import com.a404.duckonback.dto.UserDTO;
import com.a404.duckonback.entity.User;
import com.a404.duckonback.enums.PenaltyStatus;
import com.a404.duckonback.enums.PenaltyType;
import com.a404.duckonback.enums.UserRole;
import com.a404.duckonback.exception.CustomException;
import com.a404.duckonback.repository.UserRepository;
import com.a404.duckonback.util.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final ArtistService artistService;
    private final ArtistFollowService artistFollowService;

    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        String email = loginRequest.getEmail();
        String userId = loginRequest.getUserId();
        String password = loginRequest.getPassword();

        if((email != null && !email.isBlank()) && (userId != null && !userId.isBlank())){
            throw new CustomException("email 또는 userId 중 하나만 입력해야합니다.", HttpStatus.BAD_REQUEST);
        }

        if ((email == null || email.isBlank()) && (userId == null || userId.isBlank())) {
            throw new CustomException("email 또는 userId 중 하나는 필수입니다.", HttpStatus.BAD_REQUEST);
        }

        if (password == null || password.isBlank()) {
            throw new CustomException("비밀번호는 필수 입력입니다.", HttpStatus.BAD_REQUEST);
        }

        User user = null;
        if (email != null && !email.isBlank()) {
            user = userService.findByEmail(email);
        } else if (userId != null && !userId.isBlank()) {
            user = userService.findByUserId(userId);
        }

        if (user == null) {
            throw new CustomException("존재하지 않는 사용자입니다.", HttpStatus.UNAUTHORIZED);
        }


        LocalDateTime now = LocalDateTime.now();
        boolean isSuspended = user.getPenalties().stream()
                .anyMatch(p -> p.getPenaltyType() == PenaltyType.ACCOUNT_SUSPENSION
                        && (p.getStartAt() == null || !p.getStartAt().isAfter(now))
                        && (p.getEndAt() == null || p.getEndAt().isAfter(now))
                        && (p.getStatus() == PenaltyStatus.ACTIVE));

        if (isSuspended) {
            throw new CustomException("계정이 정지되었습니다. 고객센터에 문의하세요.", HttpStatus.FORBIDDEN);
        }

        //비밀번호 일치 확인 ( 암호화 )
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        //토근 생성
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);
        
        //(추가 필요 )redis에 저장

        UserDTO userDTO = UserDTO.builder()
                .email(user.getEmail())
                .userId(user.getUserId())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .role(user.getRole().name())
                .language(user.getLanguage())
                .imgUrl(user.getImgUrl())
                .artistList(artistService.findAllArtistIdByUserId(user.getId()))
                .build();

        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userDTO)
                .build();
    }

    @Override
    public ResponseEntity<?> signup(SignupRequestDTO dto){
        MultipartFile file = dto.getProfileImg();
        String imgUrl = null;
        if (file != null && !file.isEmpty()) {
            //이미지 저장
        }

        User user = User.builder()
                .email(dto.getEmail())
                .userId(dto.getUserId())
                .password(passwordEncoder.encode(dto.getPassword()))
                .nickname(dto.getNickname())
                .createdAt(LocalDateTime.now())
                .role(UserRole.USER)
                .language(dto.getLanguage())
                .imgUrl(imgUrl)
                .build();

        userService.save(user);

        if (dto.getArtistList() != null && !dto.getArtistList().isEmpty()) {
            artistFollowService.followArtists(user.getId(), dto.getArtistList());
        }

        return ResponseEntity.ok().body("회원가입이 성공적으로 완료되었습니다!");
    }

    @Override
    public String refreshAccessToken(String refreshTokenHeader){
                // Bearer 제거
        if (!refreshTokenHeader.startsWith("Bearer ")) {
            throw new CustomException("잘못된 형식의 토큰입니다.", HttpStatus.BAD_REQUEST);
        }

        String refreshToken = refreshTokenHeader.substring(7);

        if (!jwtUtil.validateToken(refreshToken)) {
            throw new CustomException("유효하지 않은 Refresh Token입니다.", HttpStatus.UNAUTHORIZED);
        }

        Claims claims = jwtUtil.getClaims(refreshToken);
        String userId = claims.getSubject();

        // (추가 필요 ) redis에 저장된 refreshToken과 비교


        User user = userService.findByUserId(userId);
        return jwtUtil.generateAccessToken(user);
    }


    @Override
    public void logout(User user, String refreshToken) {
        //String accessToken = jwtUtil.resolveCurrentToken(); // SecurityContext 기반으로 현재 accessToken 추출

        //accessToken, refreshToken 블랙리스트 중복등록 방지 +등록 필요



    }
}
