# 🐤 DuckOn — 영상 동시 시청과 실시간 채팅을 지원하는 글로벌 팬 플랫폼

> 전 세계 K-POP 팬들과 **같은 시간, 같은 화면**을 보며 함께 이야기하세요!
> 팬심은 실시간으로 나눌 때 가장 뜨겁습니다 🔥

**DuckOn** 은 유튜브 영상을 **동시에 시청**하고 **실시간 채팅**으로 소통하는 팬 커뮤니티 플랫폼입니다.
기존 1:1/게시판 중심의 커뮤니티와 달리, **영상 시청 + 단체 채팅**이라는 몰입형 경험을 제공합니다.

* 🔗 **바로 사용하기:** [https://duckon.site](https://duckon.site)
* 📮 **피드백/문의:** [https://forms.gle/XeoBXDKUCksSiNKx9](https://forms.gle/XeoBXDKUCksSiNKx9)
* 📄 **팀 노션:** [https://minjoon98.notion.site/a404](https://minjoon98.notion.site/a404)

---

## 📌 핵심 기능

* 방 생성/참여/퇴장, **유튜브 동시 시청**
* **실시간 채팅**(아티스트 채팅/방 채팅)
* **플레이리스트 관리**(➕추가, **↕️ 순서 변경**, **🗑 삭제**)
* 방장 권한(위임/방 삭제), 유저 신고
* 팔로우/언팔로우/목록
* 아티스트 목록/상세
* 이미지 업로드(S3)
* (개발 완료, 운영 미적용) **채팅 번역** *(메모리 이슈로 운영 비활성)*

---

## 🛠 기술 스택

### Backend

* **Java 21**, **Spring Boot 3.5.3**
* JPA + **MySQL 8.0**
* **MongoDB** (채팅 로그)
* **Redis** (실시간 방/채팅 캐시)
* **WebSocket** (SockJS/STOMP)
* OAuth2 + **JWT**
* AWS S3 (이미지 저장)
* **Docker**, GitLab Runner CI/CD

**개발 환경**

* IDE: **IntelliJ IDEA 2025.1.3**
* Gradle Wrapper 사용

**주요 의존성 (build.gradle)**:

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.3'
    id 'io.spring.dependency-management' version '1.1.7'
}

group = 'com.A404'
version = '0.0.1-SNAPSHOT'

java {
    toolchain { languageVersion = JavaLanguageVersion.of(21) }
}

repositories { mavenCentral() }

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-security'
    implementation 'org.springframework.boot:spring-boot-starter-oauth2-client'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'org.springframework.boot:spring-boot-starter-data-redis'
    implementation 'org.springframework.boot:spring-boot-starter-data-mongodb'
    implementation 'org.springframework.boot:spring-boot-starter-webflux'

    implementation 'io.jsonwebtoken:jjwt-api:0.12.5'
    runtimeOnly  'io.jsonwebtoken:jjwt-impl:0.12.5'
    runtimeOnly  'io.jsonwebtoken:jjwt-jackson:0.12.5'

    implementation 'com.mysql:mysql-connector-j:9.3.0'
    implementation 'com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.19.1'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.9'

    implementation 'software.amazon.awssdk:s3:2.32.10'
    implementation 'software.amazon.awssdk:auth:2.32.10'

    implementation 'com.github.pemistahl:lingua:1.2.2'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'com.github.ben-manes.caffeine:caffeine:3.1.8'

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}
```

### Frontend

* **React 19 + TypeScript 5.8**, **Vite 7**
* 상태/데이터: **@tanstack/react-query**, **zustand**
* 통신: **axios**, **sockjs-client**, **@stomp/stompjs**
* UI: **TailwindCSS 4**, **@headlessui/react**, **lucide-react**
* 미디어: **react-youtube**

**개발 환경**

* IDE: **VS Code**

**주요 의존성 (package.json 요약)**:

```json
{
  "dependencies": {
    "@stomp/stompjs": "^7.1.1",
    "@tanstack/react-query": "^5.84.2",
    "axios": "^1.11.0",
    "react": "^19.1.0",
    "react-dom": "^19.1.0",
    "react-router-dom": "^7.7.0",
    "react-youtube": "^10.1.0",
    "sockjs-client": "^1.6.1",
    "zustand": "^5.0.6"
  },
  "devDependencies": {
    "@vitejs/plugin-react": "^4.6.0",
    "tailwindcss": "^4.1.11",
    "typescript": "~5.8.3",
    "vite": "^7.0.4"
  }
}
```

---

## 🧱 아키텍처 & 인프라

* **Reverse Proxy:** Nginx (`duckon-proxy`)
* **Prod 컨테이너 예시**

  * `duckon-front:prod` → Nginx 정적서버
  * `duckonback:prod`  → Spring Boot
  * `mysql:8.0`        → 3307(host)→3306(container)
  * `mongo:latest`     → 27017
  * `redis:7-alpine`   → prod(6379, localhost 바인드)
* **Dev 컨테이너 예시**

  * `duckon-front:latest` (8081→80)
  * `duckonback:latest`  (8080→8080)
  * `duckon-translate:latest` (8089) *— 운영 비활성 권장(메모리)*

> 배포는 **GitLab Runner**로 자동화. 빌드된 이미지를 원격 EC2에서 컨테이너로 기동합니다.

---

## 🔐 환경 변수 (요약)

*Backend (예시)*

```
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-prod:3306/duckon?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=duckon
SPRING_DATASOURCE_PASSWORD=********
REDIS_HOST=redis-prod
REDIS_PORT=6379
MONGO_URI=mongodb://duckon-mongo:27017/duckon
JWT_SECRET=***  # + access/refresh 만료시간
AWS_REGION=ap-northeast-2
AWS_ACCESS_KEY_ID=***
AWS_SECRET_ACCESS_KEY=***
S3_BUCKET=duckon-bucket
BASE_URL=https://duckon.site

# OAuth2
OAUTH_GOOGLE_CLIENT_ID=***
OAUTH_GOOGLE_CLIENT_SECRET=***
OAUTH_KAKAO_CLIENT_ID=***
OAUTH_KAKAO_CLIENT_SECRET=***
OAUTH_NAVER_CLIENT_ID=***
OAUTH_NAVER_CLIENT_SECRET=***
```

*Frontend (Vite)*

```
VITE_API_BASE_URL=/api
```

*Translate (선택 운영 비권장)*

```
MODEL_ID=facebook/m2m100_418M
HF_TOKEN=***
REDIS_URL=redis://redis-prod:6379/0
CACHE_TTL_SECONDS=604800
MAX_CHARS=1000
```

---

## 🚀 로컬/서버 실행 (요약)

> 실제 배포는 GitLab Runner로 자동화. 아래는 단일 노드 예시입니다.

```bash
# 공용 네트워크
docker network create duckon-net || true

# Front/Back
docker run -d --name duckon-front-prod --network duckon-net duckon-front:prod
docker run -d --name duckon-app-prod   --network duckon-net duckonback:prod

# DB/스토리지
docker run -d --name mysql-prod  -p 3307:3306 --network duckon-net \
  -e MYSQL_ROOT_PASSWORD=*** -e MYSQL_DATABASE=duckon mysql:8.0
docker run -d --name duckon-mongo -p 27017:27017 --network duckon-net mongo:latest
docker run -d --name redis-prod   -p 127.0.0.1:6379:6379 --network duckon-net redis:7-alpine

# 프록시
docker run -d --name duckon-proxy -p 80:80 -p 443:443 --network duckon-net nginx:alpine
```

Nginx 리버스 프록시(요지):

```nginx
location /      { proxy_pass http://duckon-front-prod:80; }
location /api   { proxy_pass http://duckon-app-prod:8080; }
location /ws-chat {
  proxy_http_version 1.1;
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection "upgrade";
  proxy_pass http://duckon-app-prod:8080;
}
```

---

## 🗂 DB & 덤프

* **RDB (MySQL):** 관계 데이터(회원/팔로우/방/아티스트 등)
* **MongoDB:** 채팅 로그

**덤프 예시(레포 동봉 권장)**

* `exec/db/schema.sql` — 테이블 DDL
* `exec/db/duckon_prod_artist.sql` — **artist 데이터 덤프**(DDL 제외, INSERT만)

**백업/복구**

```bash
# MySQL (스키마+데이터 백업)
mysqldump -h <host> -P <port> -u <user> -p --databases duckon > exec/db/mysql_duckon_YYYYMMDD.sql
# 복구
mysql -h <host> -P <port> -u <user> -p < exec/db/mysql_duckon_YYYYMMDD.sql

# MongoDB
mongodump   --uri="mongodb://<host>:27017/duckon" --out exec/db/mongo_YYYYMMDD/
mongorestore --uri="mongodb://<host>:27017/" exec/db/mongo_YYYYMMDD/
```

---

## 👥 팀 구성

| 이름  | 역할 | 주요 담당                                           |
| --- | -- | ----------------------------------------------- |
| 문준범 | BE | 실시간 영상 제어(WebSocket+Iframe), 플레이리스트, 채팅 연동, JWT |
| 김민준 | LEADER, BE | 전반 API, 실시간 방 목록 최적화, OAuth2, 채팅, 번역                |
| 김예진 | BE, INFRA | 회원/팔로우 API, JWT 블랙리스트(탈퇴/로그아웃), CI/CD           |
| 김미진 | FE | 아티스트 채팅 UI, 소셜 로그인, 채팅 통신, 아티스트 연동              |
| 박민수 | FE | 방 생성/목록/상세, 팔로우/마이페이지, 영상 공유 UI                 |

---

## 📦 릴리즈/운영 메모

* 기능 구현: **완료**
* 운영: 번역 서버는 **메모리 사용량 이슈**로 비활성. (FastAPI 서비스/모델 준비 완료)
* Nginx `client_max_body_size` 필요 시 상향(예: 20m)
* OAuth 리다이렉트: `https://duckon.site/login/oauth2/code/{google|naver|kakao}`

---

## 📝 라이선스/3rd-party

Spring Boot, MySQL/MongoDB/Redis Docker, AWS SDK v2, springdoc, jjwt, lingua, Caffeine
번역(개발용): HuggingFace Transformers, Marian, M2M100 *(모델/라이선스 준수)*

---

### Appendix — 번역 서비스(개발용) 개요

* FastAPI(Uvicorn), `facebook/m2m100_418M` + Marian 라우팅, Redis 캐시
* 용어집/강제어(아이돌 도메인 용어) 후처리/휴리스틱 적용
* `/translate`, `/translate/batch`, `/health`, `/ready`
* 운영 환경에서는 **비활성** 또는 경량 모델/문장 길이 제한 권장

> 서버 코드: `duckon-translate/server.py` (환경변수: `MODEL_ID`, `HF_TOKEN`, `REDIS_URL`, `MAX_CHARS` 등)
