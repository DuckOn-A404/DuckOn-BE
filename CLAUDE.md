# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소에서 작업할 때 참고하는 가이드입니다.

## 빌드 & 실행 명령어

```bash
# 빌드
./gradlew build
./gradlew clean build

# 실행
./gradlew bootRun

# 전체 테스트
./gradlew test

# 단일 테스트 클래스 또는 메서드 실행
./gradlew test --tests "com.a404.duckonback.SomeTest"
./gradlew test --tests "com.a404.duckonback.SomeTest.methodName"
```

## 아키텍처 개요

**DuckOn**은 Spring Boot 3.5.3 / Java 21로 개발된 K-POP 팬 커뮤니티 백엔드입니다.

### 패키지 구조

```
src/main/java/com/a404/duckonback/
├── common/
│   ├── config/          # Spring Security, Redis, WebSocket, S3, CORS 설정
│   ├── dto/             # 공유 DTO (인증, 페이지네이션 등)
│   ├── entity/          # BaseLastModifiedAuditEntity (공통 감사 필드)
│   ├── enums/           # SocialProvider, UserRole 등 공통 열거형
│   ├── exception/       # CustomException, GlobalExceptionHandler
│   ├── filter/          # JWTFilter, OAuth2 인증 필터
│   ├── handler/         # 인증 성공/실패 핸들러
│   ├── infra/
│   │   ├── redis/       # RedisService 구현체
│   │   └── s3/          # S3Service + 파일 유효성 검사
│   ├── interceptor/     # JwtHandshakeInterceptor, WsAccessInterceptor, ApiBrowserRedirectInterceptor
│   ├── notification/
│   │   └── email/       # EmailSender, EmailVerificationService
│   ├── oauth/           # OAuth2 프로바이더별 사용자 정보 파싱
│   ├── response/        # ApiResponseDTO, ErrorCode, SuccessCode 열거형
│   ├── security/
│   │   └── token/       # TokenBlacklistService (Redis 기반 JWT 블랙리스트)
│   ├── util/            # JWTUtil, CookieUtil, GuestNicknameGenerator, ChatRateLimiter 등
│   └── validation/      # 커스텀 제약 어노테이션 (NullOrNotBlank 등)
│
└── domain/
    ├── admin/           # 관리자 기능
    ├── artist/
    │   ├── artist/      # 아티스트 CRUD (controller/dto/entity/repository/service)
    │   ├── common/      # ArtistReadable 인터페이스, ArtistSummaryDTO (도메인 내 공유)
    │   ├── emerging/    # 신인 아티스트
    │   ├── promotion/   # 아티스트 프로모션 (service만 존재, 별도 controller 없음)
    │   └── request/     # 아티스트 등록 요청
    ├── auth/            # 로그인, 회원가입, 토큰 갱신
    ├── chat/            # WebSocket 채팅 (MongoDB 저장)
    ├── home/            # 홈 화면, 검색 플레이스홀더 관리
    ├── me/              # 내 프로필, 설정
    ├── meme/            # 밈 CRUD, 즐겨찾기, 사용 로그
    ├── notification/    # 알림 (도메인 알림; 이메일은 common/notification)
    ├── penalty/         # 제재 관리
    ├── report/          # 신고 처리
    ├── room/            # 채팅방 관리
    ├── tag/             # 태그
    ├── translation/     # 외부 번역 서비스 연동
    ├── upload/          # S3 파일 업로드 (UploadPurpose 열거형으로 목적 구분)
    ├── user/            # 사용자 계정 관리
    └── youtube/         # YouTube API 연동
```

각 `domain/{feature}/`는 아래 구조를 따릅니다:
```
controller/   → @RestController
service/      → 인터페이스 + Impl
dto/          → *RequestDTO, *ResponseDTO
entity/       → @Entity
repository/   → JpaRepository + 커스텀 @Query
```

### 주요 기술 스택

| 레이어 | 기술 |
|--------|------|
| 주 DB | MySQL 8 (JPA/Hibernate) |
| 문서 저장소 | MongoDB (채팅 기록) |
| 캐시 / 블랙리스트 | Redis 7 + Caffeine (로컬) |
| 인증 | Spring Security 6, JWT (JJWT 0.12.5), OAuth2 (Google/Kakao/Naver) |
| 실시간 채팅 | WebSocket + STOMP (`/ws-chat`) via SockJS |
| 파일 저장 | AWS S3 SDK v2 |
| API 문서 | SpringDoc OpenAPI → `/swagger-ui.html` |

### 인증 흐름

1. **JWT**: 액세스 토큰 (15분) 은 응답 바디로 반환, 리프레시 토큰 (2주) 은 HttpOnly 쿠키로 전달.
2. **토큰 갱신**: `POST /api/auth/refresh` 로 새 액세스 토큰 발급.
3. **로그아웃**: 토큰을 Redis 블랙리스트(`common/security/token/`)에 추가 후 쿠키 삭제.
4. **OAuth2**: 각 프로바이더가 `/login/oauth2/code/{google|kakao|naver}` 로 리다이렉트; 성공/실패 시 `${BASE_URL}/oauth2/success` 또는 `/oauth2/failure` 로 이동.

### 응답 & 에러 컨벤션

모든 엔드포인트는 `ApiResponseDTO` 형식으로 응답합니다:
```json
{ "status": 200, "message": "...", "data": { ... } }
```

비즈니스 에러는 `CustomException(ErrorCode.SOME_CODE)` 를 던지면 됩니다. `GlobalExceptionHandler` (@RestControllerAdvice) 가 이를 포함한 Spring 표준 예외를 통합 응답 형식으로 변환합니다.

새 코드는 `common/response/` 의 `ErrorCode` 또는 `SuccessCode` 열거형에 추가합니다.

### 엔티티 컨벤션

- 공통 감사 필드(`createdAt`, `updatedAt`)는 `common/entity/BaseLastModifiedAuditEntity` 상속.
- 소프트 삭제: `boolean deleted` + `deletedAt` — 쿼리에서 항상 `deleted = false` 필터 적용.
- 열거형은 `@Enumerated(EnumType.STRING)` 으로 저장.
- N+1 문제 방지를 위해 `@Query` JPQL에 left join fetch 사용.

### 환경 변수

모든 시크릿은 환경 변수로 주입됩니다. 주요 그룹:

| 그룹 | 변수 |
|------|------|
| MySQL | `SPRING_DATASOURCE_URL`, `_USERNAME`, `_PASSWORD` |
| Redis | `REDIS_HOST`, `REDIS_PORT` |
| MongoDB | `MONGO_DB_URL`, `_USERNAME`, `_PASSWORD`, `_NAME` |
| JWT | `JWT_SECRET` |
| OAuth2 | `GOOGLE_ID/SECRET`, `KAKAO_ID/SECRET`, `NAVER_ID/SECRET` |
| S3 | `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_REGION`, `S3_BUCKET_NAME` |
| 이메일 (SMTP) | `SMTP_SERVER`, `SMTP_PORT`, `SMTP_ID`, `SMTP_PW`, `FROM_EMAIL` |
| 외부 서비스 | `TRANSLATE_BASE_URL`, `YOUTUBE_API_KEY`, `BASE_URL` |

### DTO 네이밍

패턴: `{엔티티}{작업}{방향}DTO`

예시: `LoginRequestDTO`, `LoginResponseDTO`, `ChatMessageRequestDTO`, `ChatMessageResponseDTO`.
