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
import com.a404.duckonback.util.JWTUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final ArtistService artistService;
    private final ArtistFollowService artistFollowService;
    private final S3Service s3Service;

    private final PasswordEncoder passwordEncoder;
    private final JWTUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;

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
    public ResponseEntity<?> signup(SignupRequestDTO dto) {
        MultipartFile file = dto.getProfileImg();
        String imgUrl = null;
        if (file != null && !file.isEmpty()) {
            imgUrl = s3Service.uploadFile(file);
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
    @Transactional
    public void logout(User user, String refreshHeader) {
        final long now = System.currentTimeMillis();

        // 1) Access Token: Authorization 헤더에서 안전하게 추출
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            String authHeader = attrs.getRequest().getHeader("Authorization");
            String accessToken = stripBearer(authHeader); // "Bearer "가 있든 없든 안전 추출
            blacklistIfValid(accessToken, now);
        }

        // 2) Refresh Token: 컨트롤러에서 전달받은 X-Refresh-Token (보통 Bearer 없음)
        String refreshToken = stripBearer(refreshHeader);
        blacklistIfValid(refreshToken, now);
    }

    /** "Bearer xxx" -> "xxx", null/공백 처리 포함 */
    private String stripBearer(String header) {
        if (header == null) return null;
        String h = header.trim();
        if (h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return h.substring(7).trim();
        }
        return h; // 원래 Bearer가 없으면 그대로 토큰으로 간주
    }

    // 유효 토큰만 TTL 계산해서 블랙리스트 등록 (예외/음수 TTL 방지)
    private void blacklistIfValid(String token, long now) {
        if (token == null || token.isBlank()) return;
        try {
            if (!jwtUtil.validateToken(token)) return; // 만료/위조 등은 스킵
            Date exp = jwtUtil.getClaims(token).getExpiration();
            long ttl = exp.getTime() - now;
            if (ttl > 0) {
                tokenBlacklistService.blacklist(token, ttl);
            }
        } catch (Exception ignore) {
            // 파싱 실패/예외는 조용히 무시 (로그아웃은 idempotent하게)
        }
    }
}
