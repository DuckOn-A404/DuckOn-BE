package com.a404.duckonback.domain.auth.service;

import com.a404.duckonback.common.dto.JWTDTO;
import com.a404.duckonback.common.enums.TokenStatus;
import com.a404.duckonback.common.response.ErrorCode;
import com.a404.duckonback.common.util.CookieUtil;
import com.a404.duckonback.domain.artist.artist.service.ArtistFollowService;
import com.a404.duckonback.domain.artist.artist.service.ArtistService;
import com.a404.duckonback.domain.auth.dto.LoginRequestDTO;
import com.a404.duckonback.domain.auth.dto.LoginResponseDTO;
import com.a404.duckonback.domain.auth.dto.RefreshResponseDTO;
import com.a404.duckonback.domain.auth.dto.SignupRequestDTO;
import com.a404.duckonback.domain.user.dto.UserDTO;
import com.a404.duckonback.domain.user.entity.User;
import com.a404.duckonback.common.enums.PenaltyStatus;
import com.a404.duckonback.common.enums.PenaltyType;
import com.a404.duckonback.common.enums.UserRole;
import com.a404.duckonback.common.exception.CustomException;
import com.a404.duckonback.common.infra.s3.S3Service;
import com.a404.duckonback.common.security.token.TokenBlacklistService;
import com.a404.duckonback.domain.user.service.UserService;
import com.a404.duckonback.common.util.JWTUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
    private final JWTUtil jWTUtil;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public JWTDTO getJWT(String email){
        User user = userService.findByEmail(email);

        String accessToken  = jWTUtil.generateAccessToken(user);
        String refreshToken = jWTUtil.generateRefreshToken(user);

        return JWTDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }


    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        String email  = loginRequest.getEmail()  == null ? null : loginRequest.getEmail().trim();
        String userId = loginRequest.getUserId() == null ? null : loginRequest.getUserId().trim();
        String password = loginRequest.getPassword();

        if (email != null && !email.isBlank() && userId != null && !userId.isBlank()) {
            throw new CustomException("email 또는 userId 중 하나만 입력해야합니다.", HttpStatus.BAD_REQUEST);
        }
        if ((email == null || email.isBlank()) && (userId == null || userId.isBlank())) {
            throw new CustomException("email 또는 userId 중 하나는 필수입니다.", HttpStatus.BAD_REQUEST);
        }
        if (password == null || password.isBlank()) {
            throw new CustomException("비밀번호는 필수 입력입니다.", HttpStatus.BAD_REQUEST);
        }

        // 🔹 탈퇴(false)인 사용자만 찾는 서비스 메서드 사용
        User user = (email != null && !email.isBlank())
                ? userService.findActiveByEmail(email)      // 내부적으로 findByEmailAndDeletedFalse 사용
                : userService.findActiveByUserId(userId);   // 내부적으로 findByUserIdAndDeletedFalse 사용

        // (이중안전) 한 번 더 체크
        if (user.isDeleted()) {
            throw new CustomException("탈퇴한 계정입니다.", HttpStatus.UNAUTHORIZED);
        }

        // 정지 여부 체크
        LocalDateTime now = LocalDateTime.now();
        boolean isSuspended = user.getPenalties().stream().anyMatch(p ->
                p.getPenaltyType() == PenaltyType.ACCOUNT_SUSPENSION
                        && (p.getStartAt() == null || !p.getStartAt().isAfter(now))
                        && (p.getEndAt() == null || p.getEndAt().isAfter(now))
                        && p.getStatus() == PenaltyStatus.ACTIVE
        );
        if (isSuspended) {
            throw new CustomException("계정이 정지되었습니다. 고객센터에 문의하세요.", HttpStatus.FORBIDDEN);
        }

        // 소셜계정 비밀번호 로그인 차단(정책에 맞게)
        if (Boolean.FALSE.equals(user.getHasLocalCredential())) {
            throw new CustomException("소셜 계정은 비밀번호 로그인을 사용할 수 없습니다.", HttpStatus.UNAUTHORIZED);
        }

        // 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new CustomException("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        // 토큰 발급
        String accessToken  = jWTUtil.generateAccessToken(user);
        String refreshToken = jWTUtil.generateRefreshToken(user);

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
    public RefreshResponseDTO refreshJWT(HttpServletRequest request, HttpServletResponse response) {
        final long now = System.currentTimeMillis();

        String refreshToken = CookieUtil.getCookieValue(request, "refreshToken");
        if(refreshToken == null || refreshToken.isBlank()) {
            throw new CustomException(ErrorCode.USER_NOT_AUTHENTICATED);
        }

        TokenStatus status = jWTUtil.getTokenValidationStatus(refreshToken);
        boolean revoked = tokenBlacklistService.isBlacklisted(refreshToken);

        if(status != TokenStatus.VALID || revoked){
            CookieUtil.deleteCookie(response, "refreshToken", isHttps(request), "Lax");
            throw new CustomException(ErrorCode.USER_NOT_AUTHENTICATED);
        }

        Claims claims = jWTUtil.getClaims(refreshToken);
        String userId = claims.getSubject();

        long ttl = claims.getExpiration().getTime() - now;
        if (ttl > 0) tokenBlacklistService.blacklist(refreshToken, ttl);

        User user = userService.findActiveByUserId(userId);

        String newAccessToken  = jWTUtil.generateAccessToken(user);
        String newRefreshToken = jWTUtil.generateRefreshToken(user);

        CookieUtil.setHttpOnlyCookie(response, "refreshToken", newRefreshToken, isHttps(request), "Lax", null);

        return new RefreshResponseDTO(newAccessToken);
    }


    @Override
    @Transactional
    public void logout(User user, HttpServletRequest request, HttpServletResponse response) {
        final long now = System.currentTimeMillis();

        // access 블랙리스트
        String authorization = request.getHeader("Authorization");
        String accessToken = jWTUtil.normalizeIfValid(authorization);

        if(accessToken != null && !accessToken.isEmpty()) {
            Date expiration = jWTUtil.getClaims(accessToken).getExpiration();
            long ttl = expiration.getTime() - now;
            if (ttl > 0) tokenBlacklistService.blacklist(accessToken, ttl);
        }

        // refresh 쿠키 삭제
        String refreshToken = CookieUtil.getCookieValue(request, "refreshToken");
        if(refreshToken != null && !refreshToken.isBlank()
            && jWTUtil.getTokenValidationStatus(refreshToken).equals(TokenStatus.VALID)) {
            Date expiration = jWTUtil.getClaims(refreshToken).getExpiration();
            long ttl = expiration.getTime() - now;
            if (ttl > 0) tokenBlacklistService.blacklist(refreshToken, ttl);
        }

        // 쿠키 삭제
        CookieUtil.deleteCookie(response, "refreshToken", isHttps(request), "Lax");
    }

    private boolean isHttps(HttpServletRequest request) {
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (forwardedProto != null) return "https".equalsIgnoreCase(forwardedProto);
        return request.isSecure();
    }

}
