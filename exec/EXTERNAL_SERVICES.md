# DuckOn 외부 서비스 정보

> — 프로젝트에서 사용하는 외부 서비스(소셜 인증, 스토리지, 번역 API 등)의 **가입·설정·연동 방법**을 정리했습니다. 실제 값(키/시크릿)은 업로드하지 마시고, 이 문서의 체크리스트에 따라 각 콘솔에서 발급·설정 후 `env.properties` 및 서버 ENV에 주입하세요.

---

## 목차

1. [소셜 로그인 (Google / Naver / Kakao)](#소셜-로그인-google--naver--kakao)
2. [AWS S3 (이미지 업로드/정적자원)](#aws-s3-이미지-업로드정적자원)
3. [번역 서비스 (HuggingFace + FastAPI)](#번역-서비스-huggingface--fastapi)
4. [YouTube 플레이어](#youtube-플레이어)
5. [환경변수 요약 표](#환경변수-요약-표)
6. [보안·운영 체크리스트](#보안운영-체크리스트)

---

## 소셜 로그인 (Google / Naver / Kakao)

### 공통

* **Redirect URI(반드시 등록)**: `https://duckon.site/login/oauth2/code/{google|naver|kakao}`
* **로그인 시작 URL(참고)**: `https://duckon.site/oauth2/authorization/{google|naver|kakao}` (Spring Security 기본)
* **허용 도메인/오리진**: `duckon.site`, `www.duckon.site`
* **환경변수 매핑**: `env.properties`의 `GOOGLE_ID/SECRET`, `NAVER_ID/SECRET`, `KAKAO_ID/SECRET`, `BASE_URL`

> ⚠️ **HTTPS 필수**: 소셜 인증은 대부분 HTTPS 환경을 요구합니다. 운영 도메인에 TLS 인증서가 적용되어 있어야 합니다.

### 1) Google

1. **Google Cloud Console** → 새 프로젝트 생성 → *OAuth 동의 화면* 구성

   * 사용자 유형: External(외부) → 앱 정보 및 범위(`email`, `profile`) 추가
2. **API 및 서비스 → 사용자 인증 정보**

   * **OAuth 클라이언트 ID 만들기** → *웹 애플리케이션*
   * *승인된 JavaScript 원본*: `https://duckon.site`
   * *승인된 리디렉션 URI*: `https://duckon.site/login/oauth2/code/google`
3. 발급된 **Client ID/Secret**을 아래 키에 주입

   * `GOOGLE_ID`, `GOOGLE_SECRET`

### 2) Naver

1. **Naver Developers** → 애플리케이션 등록

   * 서비스 URL: `https://duckon.site`
   * 로그인 오픈 API 사용: 사용
   * **Callback URL**: `https://duckon.site/login/oauth2/code/naver`
2. 검수 시 서비스 소개/화면 캡처를 요구할 수 있습니다(이름/이메일 사용 근거). 필요 시 `exec/` 폴더에 PDF/캡처 포함 권장.
3. 발급값 주입: `NAVER_ID`, `NAVER_SECRET`

### 3) Kakao

1. **Kakao Developers** → 앱 생성

   * 플랫폼 → **웹** 도메인: `https://duckon.site`
   * Redirect URI: `https://duckon.site/login/oauth2/code/kakao`
2. *보안* 탭에서 Client Secret 사용을 활성화한 경우 Secret도 발급됩니다.
3. 발급값 주입: `KAKAO_ID`(REST API Key), `KAKAO_SECRET`(선택/활성 시)

---

## AWS S3 (이미지 업로드/정적자원)

### 준비물

* AWS 계정, **리전**(예: `ap-northeast-2`), **버킷명**(예: `duckon-bucket`)
* 최소 권한의 **IAM 사용자**(Access Key/Secret Key)

### 권한 정책 예시 (IAM Policy)

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": ["s3:PutObject", "s3:GetObject", "s3:DeleteObject"],
      "Resource": ["arn:aws:s3:::duckon-bucket/*"]
    },
    {
      "Effect": "Allow",
      "Action": ["s3:ListBucket"],
      "Resource": ["arn:aws:s3:::duckon-bucket"]
    }
  ]
}
```

### 버킷 CORS 설정 예시

```json
[
  {
    "AllowedHeaders": ["*"],
    "AllowedMethods": ["GET", "PUT", "POST", "DELETE"],
    "AllowedOrigins": ["https://duckon.site", "https://www.duckon.site"],
    "ExposeHeaders": ["ETag"],
    "MaxAgeSeconds": 3000
  }
]
```

### 애플리케이션 연동

* 라이브러리: **AWS SDK v2** (`software.amazon.awssdk:s3:2.32.10`)
* 환경변수: `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_REGION`, `S3_BUCKET_NAME`
* 업로드 경로/권한은 애플리케이션 정책에 맞춰 제한(예: `user-uploads/`) 권장

---

## 번역 서비스 (HuggingFace + FastAPI)

> 운영 환경에서는 **리소스 사용량** 문제로 비활성화할 수 있습니다. 도입 시 아래 절차를 따르세요.

### 준비물

* **HuggingFace 계정** 및 **Personal Access Token** → `HF_TOKEN`
* 기본 모델: `facebook/m2m100_418M` (필요 시 Marian 라우팅: ko↔en/ja/zh 등)
* 선택: **Redis** 캐시(성능 향상) — `REDIS_URL` (예: `redis://redis-prod:6379/0`)

### 서버(duckon-translate) 주요 ENV

* `MODEL_ID`, `HF_TOKEN`, `REDIS_URL`, `CACHE_TTL_SECONDS`, `MAX_CHARS`, `MARIAN_OFF` 등
* 앱 포트: `8089` (예시)

### API 인터페이스

* **POST** `/translate`

  * 요청: `{ "text": "안녕", "src": "ko", "tgt": "en", "use_glossary": true }`
  * 응답: `{ "translation": "Hello" }`
  * 응답 헤더: `X-Translate-Model`, `X-Cache`
* **POST** `/translate/batch` — `items` 배열로 다중 번역
* **GET** `/health`, `/ready` — 상태 점검

### 운영 팁

* `MAX_CHARS`(입력 길이), `force_words` 제한으로 OOM 방지
* GPU 미탑재 시 CPU로 동작(지연 증가) → 캐시/큐잉 고려

---

## YouTube 플레이어

* **현 버전**: *임베디드 플레이어*만 사용 (`react-youtube`), **YouTube Data API 키 불필요**
* 동작 원리: 영상 ID로 `<iframe>` 임베드 → 동일 시청을 위해 방 단위로 플레이/시점 동기화
* 만약 Data API를 도입해 검색/메타데이터를 확장할 경우:

  * Google Cloud Console → **YouTube Data API v3** 활성화 → API 키 발급 → 서버 보관/레퍼러 제한 적용

---

## 환경변수 요약 표

| 구분           | 키                                                               | 설명                     | 설정 위치                        |
| ------------ | --------------------------------------------------------------- | ---------------------- | ---------------------------- |
| Google OAuth | `GOOGLE_ID`, `GOOGLE_SECRET`                                    | 구글 OAuth 클라이언트 ID/시크릿  | `env.properties` 또는 컨테이너 ENV |
| Naver OAuth  | `NAVER_ID`, `NAVER_SECRET`                                      | 네이버 앱 클라이언트 ID/시크릿     | 동일                           |
| Kakao OAuth  | `KAKAO_ID`, `KAKAO_SECRET`                                      | 카카오 REST API 키/시크릿(선택) | 동일                           |
| Base URL     | `BASE_URL`                                                      | 서비스 외부 접근 URL          | 동일                           |
| S3           | `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_REGION`, `S3_BUCKET_NAME` | S3 접근용 자격증명/리전/버킷명     | 동일                           |
| HuggingFace  | `HF_TOKEN`                                                      | 모델 다운로드용 토큰            | 번역 서버 ENV                    |
| 번역 서버        | `MODEL_ID`, `REDIS_URL`, `CACHE_TTL_SECONDS`, `MAX_CHARS` 등     | 번역 서버 동작 파라미터          | 번역 서버 ENV                    |
| Front        | `VITE_API_BASE_URL`, `VITE_WS_BASE`                             | API/WS 엔드포인트(프론트)      | `FE/.env*`                   |

> 백엔드의 DB/Redis/Mongo 설정 키는 **외부 서비스**가 아닌 내부 인프라 범주이므로 본 표에서 제외했습니다.

---

## 보안·운영 체크리스트

* **비밀값은 저장소에 커밋 금지** — GitLab CI 변수 또는 서버의 `*.env` 파일로 주입
* **OAuth Redirect URI** 정확히 일치 등록(종종 슬래시 누락으로 실패)
* **S3 퍼블릭 정책 최소화** — 필요 시 프리사인드 URL로 접근 권장
* **번역 서버 비활성/비공개** 운영 권장(메모리 사용량, 라이선스/요금 주의)
* **도메인/HTTPS** 만료 알림 설정(Let’s Encrypt 갱신 주기 확인)

