# DuckOn 포팅 매뉴얼

> 이 문서는 GitLab 저장소를 클론한 뒤, 동일한 인프라에서 **빌드·배포**할 수 있도록 정리한 포팅 매뉴얼입니다. 운영/개발 환경 모두 Docker 기반으로 구성되며, Nginx 리버스 프록시 하에 Front/Back/번역(선택) 서비스가 동작합니다.

---

## 0) 시스템 개요

* **서비스명:** *DuckOn (덕온) – 실시간 팬 커뮤니티 (유튜브 동시 시청 + 실시간 채팅)*
* ***아키텍처:***\* Nginx(리버스 프록시) ↔ Front(Nginx 정적서버) / Back(Spring Boot) / Translate(FastAPI, 선택) ↔ MySQL, MongoDB, Redis\*
* ***배포 방식:***\* GitLab Runner on EC2, Docker 컨테이너 실행\*
* ***도메인 예:***\* **`https://duckon.site`** (예시)\*

---

## 1) 개발 도구·러untime 버전 및 설정

### 1.1 IDE & 언어/런타임

* **Backend IDE:** IntelliJ IDEA **2025.1.3**

* **Java:** **JDK 21** (Gradle Toolchain 사용)

* **Spring Boot:** **3.5.3** (내장 Tomcat 10.x)

* **Gradle:** Wrapper 포함 (레포 내 gradlew 사용)

* **Frontend IDE:** VS Code (버전 무관, LTS 권장)

* **Node.js:** 20.x LTS 이상 권장

* **Vite:** ^7

* **TypeScript:** \~5.8.3

### 1.2 웹서버 / WAS

* **Nginx (reverse proxy):** `nginx:alpine` 사용
* **Front 정적서버:** `nginx:alpine` (프론트 빌드 산출물 서빙)
* **WAS:** Spring Boot 내장 Tomcat (별도 설정 불필요)

### 1.3 DB/스토리지/캐시

* **MySQL:** 8.0 (운영: 호스트 3307 → 컨테이너 3306)
* **MongoDB:** latest (채팅 등 비정형 데이터)
* **Redis:** 7-alpine (캐시/임시 데이터; dev: 6380, prod: 6379, 로컬바인드)
* **Object Storage:** AWS S3 (이미지 업로드 등)

---

## 2) 빌드 시 사용되는 환경변수 (예시)

> 실제 값은 보안상 **예시**로 표기했습니다. `exec/.env.*` 또는 GitLab CI 변수/EC2 환경변수로 주입하세요.

### 2.1 Backend (Spring Boot)

```
# 공통
SPRING_PROFILES_ACTIVE=prod
BASE_URL=https://duckon.site

# DB
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-prod:3306/duckon?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=duckon
SPRING_DATASOURCE_PASSWORD=********

# Redis
REDIS_HOST=redis-prod
REDIS_PORT=6379

# Mongo
MONGO_URI=mongodb://duckon-mongo:27017/duckon

# JWT
JWT_SECRET=***long-random-secret***
JWT_ACCESS_MINUTES=60
JWT_REFRESH_DAYS=14

# OAuth2 (예: Google/Naver/Kakao 사용 시)
OAUTH_GOOGLE_CLIENT_ID=***
OAUTH_GOOGLE_CLIENT_SECRET=***
OAUTH_NAVER_CLIENT_ID=***
OAUTH_NAVER_CLIENT_SECRET=***
OAUTH_KAKAO_CLIENT_ID=***
OAUTH_KAKAO_CLIENT_SECRET=***
# 콜백 예) https://duckon.site/login/oauth2/code/google

# CORS/프론트 URL
CORS_ALLOWED_ORIGINS=https://duckon.site,https://www.duckon.site

# S3 (AWS SDK v2)
AWS_REGION=ap-northeast-2
AWS_ACCESS_KEY_ID=***
AWS_SECRET_ACCESS_KEY=***
S3_BUCKET=duckon-bucket

# Springdoc(OpenAPI)
SPRINGDOC_SWAGGER_UI_ENABLED=true
```

#### 2.1a env.properties (실제 파일 키 명세)

다음 파일에 **정확히 아래 키**를 채워주세요.

`BE\src\main\resources\env.properties`

```properties
# ====================
# JWT Configuration
# ====================
JWT_SECRET_KEY=changeme-64bytes-random
# 단위: 밀리초(ms)
JWT_ACCESS_EXPIRATION=3600000           # 60분
JWT_REFRESH_EXPIRATION=1209600000       # 14일

# ====================
# Database Configuration
# ====================
SPRING_DATASOURCE_URL=jdbc:mysql://mysql-prod:3306/duckon?useUnicode=true&characterEncoding=utf8mb4&serverTimezone=Asia/Seoul
SPRING_DATASOURCE_USERNAME=duckon
SPRING_DATASOURCE_PASSWORD=********

# Redis
REDIS_HOST=redis-prod
REDIS_PORT=6379

# MongoDB (인증 미사용 시 ID/PW 비워두세요)
MONGO_DB_URL=mongodb://duckon-mongo:27017
MONGO_DB_NAME=duckon
MONGO_DB_USERNAME=
MONGO_DB_PASSWORD=

# ====================
# OAuth2 Configuration (Google/Naver/Kakao)
# 리다이렉트: https://duckon.site/login/oauth2/code/{google|naver|kakao}
# ====================
GOOGLE_ID=***
GOOGLE_SECRET=***
KAKAO_ID=***
KAKAO_SECRET=***
NAVER_ID=***
NAVER_SECRET=***
BASE_URL=https://duckon.site

# ====================
# S3 Configuration
# ====================
S3_ACCESS_KEY=***
S3_SECRET_KEY=***
S3_REGION=ap-northeast-2
S3_BUCKET_NAME=duckon-bucket

# ====================
# Blacklist Configuration
# ====================
BLACKLIST_HMAC_SECRET=changeme-hmac-secret

# ====================
# Translation Service Configuration (선택)
# ====================
TRANSLATE_BASE_URL=http://duckon-translate:8089
TRANSLATE_TIMEOUT_MS=2000
```

> 위 값들은 컨테이너 **ENV**로도 주입 가능합니다. (Docker `--env`/`--env-file`)  ENV가 있으면 `env.properties`보다 **우선 적용**됩니다.

### 2.2 Frontend (Vite)

`FE\.env`
```
VITE_API_BASE_URL=/api
```

### 2.3 Translate 서비스(FastAPI, 선택 배포)

```
MODEL_ID=facebook/m2m100_418M
REDIS_URL=redis://redis-prod:6379/0
CACHE_TTL_SECONDS=604800
MARIAN_OFF=0
NLLB_FORCE_WORDS=0
DEBUG=0
WARMUP_TEXT=warm up
MAX_CHARS=1000
HF_TOKEN=*** (또는 HUGGINGFACE_HUB_TOKEN)
```

> 번역 서버는 **메모리 사용량이 높을 수 있음**. CPU 전용/경량 모델, `MAX_CHARS` 축소, `force_words` 제한 등으로 튜닝하거나 미사용(프록시 경로 비활성화) 권장.

---

## 3) 빌드 & 패키징 절차

### 3.1 Backend

```
# 1) 소스 클론
$ git clone <repo-url>
$ cd backend

# 2) JDK 21 확인 후 빌드
$ ./gradlew clean build -x test
# 산출물: build/libs/app.jar (또는 {artifact}.jar)

# 3) Docker 이미지 빌드 (예)
$ docker build -t duckonback:prod .
```

### 3.2 Frontend

```
$ cd frontend
$ npm ci
$ npm run build
# 산출물: dist/

# Nginx 정적 서빙용 Docker 이미지 빌드 (예)
$ docker build -t duckon-front:prod .
```

### 3.3 Translate (선택)

> **FastAPI 번역 서버**는 별도 컨테이너로 운영합니다. 메모리 사용량에 유의하세요.

#### 3.3.1 디렉터리 구조 (예시)

* 레포 내: `duckon-translate/`
* 서버(EC2) 운영 패키지(예시): `~/translate-dev`, `~/translate-prod`

```
~/translate-dev
├─ Dockerfile
├─ requirements.txt
├─ server.py
└─ start.sh
```

#### 3.3.2 requirements.txt (고정 버전)

> 파일 위치: `duckon-translate/requirements.txt`

```text
fastapi==0.112.0
uvicorn[standard]==0.30.3
transformers==4.41.2
torch==2.3.1
accelerate==0.33.0
sentencepiece==0.2.0
redis==5.0.7
pydantic==2.8.2
langdetect==1.0.9
```

#### 3.3.3 Dockerfile (예시)

> 실제 운영 Dockerfile이 있다면 그 파일을 사용하세요. 아래는 **재현 가능한 최소 예시**입니다.

```dockerfile
# duckon-translate/Dockerfile
FROM python:3.11-slim
ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_DISABLE_PIP_VERSION_CHECK=1
WORKDIR /app

# 시스템 의존성(필요 시 추가)
RUN apt-get update && apt-get install -y --no-install-recommends \
    git \
 && rm -rf /var/lib/apt/lists/*

# 의존성 설치
COPY requirements.txt ./
RUN pip install --no-cache-dir -r requirements.txt

# 앱 소스
COPY server.py ./

# 기본 포트
EXPOSE 8089

# HEALTHCHECK(Optional): 빠른 헬스엔드포인트 사용 시
HEALTHCHECK --interval=30s --timeout=5s --retries=5 CMD curl -fsS http://127.0.0.1:8089/ready || exit 1

CMD ["uvicorn", "server:app", "--host", "0.0.0.0", "--port", "8089", "--workers", "1"]
```

#### 3.3.4 start.sh (예시)

```bash
#!/usr/bin/env bash
set -euo pipefail
uvicorn server:app --host 0.0.0.0 --port "${PORT:-8089}" --workers 1
```

> 컨테이너 CMD 대신 `start.sh`를 사용하려면 Dockerfile의 `CMD`를 `bash ./start.sh`로 교체하세요.

#### 3.3.5 빌드 & 실행

```bash
# 1) 이미지 빌드
$ cd duckon-translate
$ docker build -t duckon-translate:latest .

# 2) 실행 (공유 네트워크 사용 권장)
$ docker network create duckon-net || true
$ docker rm -f duckon-translate-dev || true
$ docker run -d --name duckon-translate-dev \
  --network duckon-net \
  -p 8089:8089 \
  -e MODEL_ID=facebook/m2m100_418M \
  -e HF_TOKEN=*** \
  -e REDIS_URL=redis://redis-prod:6379/0 \
  -e CACHE_TTL_SECONDS=604800 \
  -e MAX_CHARS=1000 \
  duckon-translate:latest

# 3) 헬스체크
$ curl -s http://localhost:8089/health | jq .
$ curl -s http://localhost:8089/ready
```

#### 3.3.6 Nginx 프록시(선택)

```nginx
# /translate 경로 프록시 (운영에서 비공개 권장)
location /translate {
    proxy_pass http://duckon-translate-dev:8089; # 컨테이너 이름/네트워크에 맞게 조정
}
```

> **주의사항**
>
> * 모델 최초 로드 시 메모리 사용량이 큽니다. EC2 사양에 맞춰 `MAX_CHARS`와 워커 수를 조정하세요.
> * HuggingFace 토큰(HF\_TOKEN)은 CI/CD 변수 또는 서버 비밀 파일로 주입하고, 저장소에 커밋하지 않습니다.

---

## 4) 런타임(배포) 구성

### 4.1 현재 컨테이너(참고)

```
# 예시 (운영 EC2)
- duckon-proxy (nginx:alpine) :80/:443
- duckon-front-prod (nginx)    :80 (내부)
- duckon-app-prod (spring)     :8080 (내부)
- duckon-translate-dev         :8089 (선택, 필요 시 프록시 연결)
- mysql-prod                   :3306 (호스트 3307)
- duckon-mongo                 :27017
- redis-prod                   :6379 (127.0.0.1 바인드)
```

### 4.2 Nginx 리버스 프록시 설정 예시

> `duckon-proxy` 컨테이너의 `/etc/nginx/conf.d/duckon.conf`

```nginx
server {
    listen 80;
    listen 443 ssl http2;  # 인증서 적용 시
    server_name duckon.site www.duckon.site;

    # SSL 인증서 (예시)
    ssl_certificate     /etc/letsencrypt/live/duckon.site/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/duckon.site/privkey.pem;

    # 업로드 제한 (413 방지)
    client_max_body_size 20m;

    # 정적(Front)
    location / {
        proxy_set_header Host $host;
        proxy_pass http://duckon-front-prod:80;
    }

    # API (Back)
    location /api {
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_pass http://duckon-app-prod:8080;
    }

    # WebSocket
    location /ws-chat {
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_pass http://duckon-app-prod:8080;
    }

    # OAuth redirect (Spring Boot가 처리)
    location /login/ {
        proxy_pass http://duckon-app-prod:8080;
    }

    # (선택) 번역 서비스 공개 시
    # location /translate {
    #     proxy_pass http://duckon-translate:8089;
    # }
}
```

### 4.3 Docker 실행 예시 (단일 노드)

```bash
# 네트워크 준비 (없으면 생성)
$ docker network create duckon-net || true

# Front/Back/DB/Cache/Mongo 실행 (이미지/이름은 운영 환경에 맞게 조정)
$ docker run -d --name duckon-front-prod --network duckon-net duckon-front:prod
$ docker run -d --name duckon-app-prod   --network duckon-net duckonback:prod
$ docker run -d --name mysql-prod  -p 3307:3306 --network duckon-net \
  -e MYSQL_ROOT_PASSWORD=*** -e MYSQL_DATABASE=duckon mysql:8.0
$ docker run -d --name duckon-mongo -p 27017:27017 --network duckon-net mongo:latest
$ docker run -d --name redis-prod   -p 127.0.0.1:6379:6379 --network duckon-net redis:7-alpine
$ docker run -d --name duckon-proxy -p 80:80 -p 443:443 --network duckon-net nginx:alpine
```

> 실제 운영에는 **GitLab Runner**를 통해 빌드/배포 자동화. Registry를 쓰는 경우 `docker login` → `docker push/pull` 구성.

### 4.4 배포 시 특이사항

* **413 Request Entity Too Large** 발생 시: `client_max_body_size` 값을 10\~50MB 수준으로 상향.
* **CORS**: 프론트 도메인(및 www 서브도메인) 모두 허용 설정 필요.
* **OAuth 리다이렉트 URI**: `https://duckon.site/login/oauth2/code/{provider}` 를 각 콘솔에 등록.
* **HTTPS 필수**: 소셜 로그인/쿠키 보안을 위해 TLS 적용 권장.
* **WebSocket**: `Upgrade/Connection` 헤더 프록시 설정 필수.
* **Translate 서비스**: 메모리 사용량 높음 → 운영에서는 **비활성** 또는 경량설정 권장.

---

### 4.5 GitLab CI/CD 배포 특이사항 (본 프로젝트)

* **브랜치 전략**

  * `BE/develop`, `FE/develop` 푸시/머지 → 각각 빌드 후 원격 EC2에 **SSH**로 접속해 `docker build` → `duckon-app-dev(8080)`, `duckon-front-dev(8081→80)` 재기동.
  * `master` → 프로덕션 빌드/배포: `duckon-app-prod`/`duckon-front-prod`를 **duckon-net** 네트워크에서 실행(프록시 뒤, 포트 바인딩 없음).
* **원격 Git 갱신**: `git remote set-url origin https://gitlab-ci-token:${CI_JOB_TOKEN}@...` 후 `git reset --hard` 사용.
  CI 토큰 만료/권한 문제 시 배포 실패 → **CI\_JOB\_TOKEN 유효성** 점검.
* **환경파일 필수**: 원격 서버에 `/home/ubuntu/dev.env`, `/home/ubuntu/prod.env` 준비.
  Spring은 컨테이너 ENV → `application.properties` → `env.properties` 순으로 덮어씀.
* **도커 네트워크**: `duckon-net` 미존재 시 생성. Nginx 업스트림 이름(예: `duckon-app-prod`, `duckon-front-prod`)과 컨테이너 이름이 **일치**해야 프록시 가능.
* **헬스체크**: 배포 스크립트에서 `https://duckon.site/api/actuator/health` HTTP 200/401 확인.
  Actuator 노출 및 프록시 경로(`/api`) 확인 필요.
* **TLS/WS**: 443 인증서 갱신 시 Nginx 재시작 필요. WebSocket은 `Upgrade/Connection` 헤더 필수.
* **Nginx 업로드 한도**: 회원가입 이미지 등 대용량 업로드 시 `client_max_body_size 20m`(또는 요구 크기) 적용.
* **DB 마이그레이션**: 최초 배포 전 `exec/db/*` 덤프 복원, MySQL **utf8mb4**, Asia/Seoul 타임존 권장.
* **보안**: Redis는 127.0.0.1 바인드, 보안그룹으로 외부 차단. SSH `known_hosts` 최신화.

## 5) 프로젝트 설정 파일 & 계정/프로퍼티 파일 목록

> **실제 비밀 값은 커밋 금지.** 아래는 위치·명칭 예시입니다.

* Backend

  * `src/main/resources/application.yml` 또는 `application.properties`
  * `env.properties` (예: 비밀키/DB 접속 등, GitLab CI 변수로 대체 가능)
* Frontend

  * `.env`, `.env.production` (Vite 변수)
* Infra

  * `nginx/conf.d/duckon.conf` (리버스 프록시)
  * `docker-compose.yml` (선택)
* 기타

  * `exec/erd.png` (ERD 이미지)
  * `exec/schema.sql` (DDL/기본데이터)

---

## 6) 외부 서비스 사용 정리

### 6.1 소셜 인증 (Google / Naver / Kakao)

**반드시 포함할 내용**

1. 각 콘솔에서 앱 생성 및 **Client ID/Secret 발급**
2. **Redirect URI** 등록: `https://duckon.site/login/oauth2/code/{google|naver|kakao}`
3. `env.properties` 키 매핑: `GOOGLE_ID/SECRET`, `NAVER_ID/SECRET`, `KAKAO_ID/SECRET`, `BASE_URL`

* **Google**: [https://console.cloud.google.com](https://console.cloud.google.com)

  * OAuth 동의화면(외부/내부), 범위: `email`, `profile`
  * 승인된 리디렉션: `.../login/oauth2/code/google`
* **Naver**: [https://developers.naver.com](https://developers.naver.com)

  * 서비스 URL/소개자료 필요. **이름(email) 활용 캡처** 첨부 요구될 수 있음(검수 반려 포인트).
  * 리디렉션: `.../login/oauth2/code/naver`
* **Kakao**: [https://developers.kakao.com](https://developers.kakao.com)

  * 플랫폼 → 웹 도메인 등록(`https://duckon.site`), Redirect: `.../login/oauth2/code/kakao`

> **포팅 매뉴얼에 꼭 명시**: 콘솔 위치, 등록할 Redirect URI, `env.properties` 키명, 필요한 권한(스코프).
> 검수 문서(특히 Naver)는 서비스 소개 PDF/캡처를 `exec/`에 포함 권장.

### 6.2 AWS S3 (이미지 업로드)

* 액세스 키/시크릿 키, 리전, 버킷명 필요

* SDK: `software.amazon.awssdk:s3:2.32.10`

* 권한 최소화(putObject/getObject), 퍼블릭 읽기 정책은 필요 최소 범위로 설정

* 액세스 키/시크릿 키, 리전, 버킷명 필요

* SDK: `software.amazon.awssdk:s3:2.32.10`

* 권한 최소화(putObject/getObject), 퍼블릭 읽기 정책은 필요 최소 범위로 설정

### 6.3 Redis / MongoDB

* Redis: 캐시/임시 저장. 로컬 바인드(127.0.0.1)로 외부 노출 최소화
* MongoDB: 채팅 등 비정형 데이터 보관

### 6.4 (선택) 번역 서비스 – HuggingFace

* 모델: `facebook/m2m100_418M` + Marian 라우팅 일부
* 토큰: `HF_TOKEN` 또는 `HUGGINGFACE_HUB_TOKEN`
* Redis 캐시 적용 (키: `mt:<route>:<sha1>:<src>:<tgt>:v7`)

---

## 7) DB 덤프 (최신본 포함)

> 덤프 파일은 `exec/db/` 디렉터리에 포함하세요.

### 7.1 MySQL 덤프/복구

```bash
# 덤프 (스키마+데이터)
$ mysqldump -h <host> -P <port> -u <user> -p --databases duckon > exec/db/mysql_duckon_YYYYMMDD.sql

# 복구
$ mysql -h <host> -P <port> -u <user> -p < exec/db/mysql_duckon_YYYYMMDD.sql
```

### 7.2 MongoDB 덤프/복구

```bash
# 덤프
$ mongodump --uri="mongodb://<host>:27017/duckon" --out exec/db/mongo_YYYYMMDD/

# 복구
$ mongorestore --uri="mongodb://<host>:27017/" exec/db/mongo_YYYYMMDD/
```

> Redis는 영속 데이터베이스가 아니므로 스냅샷 필요 시 RDB/AOF 정책에 따라 별도 백업.

---

## 8) 시연 시나리오 (스크립트)

> **목표:** “한 번에 무엇을 하는 서비스인지”를 1\~3분 내 설득력 있게 전달

1. **랜딩 진입**

   * `https://duckon.site` 접속 → 홈: 추천 아티스트/트렌딩 방 노출

2. **소셜 로그인**

   * (Google 예시) 로그인 → 상단 프로필/닉네임 표시

3. **방 생성 & 유튜브 동시 시청**

   * “방 만들기” → 방 제목 입력 → 유튜브 URL 추가
   * 다른 영상 **추가** 후 **재생 시작**

4. **플레이리스트 관리**

   * 드래그로 **순서 변경**
   * 특정 항목 **삭제**

5. **실시간 채팅**

   * 메시지 전송 → 다른 클라이언트(보조 브라우저)에서 실시간 수신 확인
   * (옵션) 번역 토글은 현재 **운영 비활성** 안내

6. **권한/안전 기능**

   * 방장 위임(또는 방 폭파), 유저 **신고** 버튼 UI 확인

7. **프로필/팔로우(있다면)**

   * 아티스트 페이지/팔로우 인터랙션 간단 소개

8. **로그아웃**

   * 세션 종료 후 메인 복귀

> 데모 전 체크리스트: OAuth 동작, Nginx 413 미발생, WS 정상, DB 연결, 이미지 업로드(S3) 확인.

---

## 9) 문제 해결(FAQ)

* **413 Request Entity Too Large**: Nginx `client_max_body_size` 상향 (예: 20m)
* **소셜 로그인 실패**: 각 콘솔 **리다이렉트 URI**/도메인 등록 재확인, HTTPS 사용
* **WS 끊김**: 프록시의 `Upgrade/Connection` 헤더 설정 확인, 보안 그룹 443 허용
* **번역 서버 메모리 초과**: 모델 경량화, `MAX_CHARS` 축소, 번역 API 경로 프록시 해제

---

## 10) GitLab Runner 간단 파이프라인 예시

> 실제 Runner는 EC2 상에서 동작 중. 아래는 참고 템플릿입니다.

```yaml
stages: [build, deploy]

build-back:
  stage: build
  script:
    - cd backend
    - ./gradlew clean build -x test
    - docker build -t registry.example.com/duckon/back:$CI_COMMIT_SHORT_SHA .
    - docker push registry.example.com/duckon/back:$CI_COMMIT_SHORT_SHA
  only: [main]

build-front:
  stage: build
  script:
    - cd frontend
    - npm ci && npm run build
    - docker build -t registry.example.com/duckon/front:$CI_COMMIT_SHORT_SHA .
    - docker push registry.example.com/duckon/front:$CI_COMMIT_SHORT_SHA
  only: [main]

deploy:
  stage: deploy
  script:
    - ssh ec2-user@<EC2_HOST> 'docker pull ... && docker compose up -d'
  only: [main]
```

---

## 11) 라이선스 고지/오픈소스

* Spring Boot, Redis/Mongo/MySQL Docker 이미지, AWS SDK v2, jjwt, springdoc, lingua, Caffeine
* 번역: HuggingFace Transformers, Marian, M2M100 (해당 라이선스 준수)

---

## 12) 부록: Backend/Frontend 의존성 요약

### Backend Gradle 주요 의존성

* `spring-boot-starter-web / data-jpa / security / oauth2-client / websocket / cache`
* DB: `mysql-connector-j:9.3.0`, `spring-boot-starter-data-mongodb`
* Auth: `io.jsonwebtoken:jjwt-* 0.12.5`
* Docs: `springdoc-openapi-starter-webmvc-ui:2.8.9`
* AWS: `software.amazon.awssdk:s3:2.32.10`, `auth:2.32.10`
* 기타: `lingua`, `caffeine`

### Frontend package.json 주요 항목

* React 19, Vite 7, TypeScript 5.8, Tailwind 4
* 상태/데이터: `@tanstack/react-query`, `zustand`, `axios`
* UI/아이콘: `@headlessui/react`, `lucide-react`
* 동시시청/통신: `react-youtube`, `sockjs-client`, `@stomp/stompjs`