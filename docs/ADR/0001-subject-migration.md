# ADR 0001 — Artist → Subject 전환

- 상태: Accepted
- 날짜: 2025-10-09
- 작성자: DuckOn-BE 팀

## 배경/문제
- 기존 `Artist` 모델은 **음악 아티스트**에 특화되어 있었고, 도메인 확장(스포츠 선수/팀, 크리에이터 등)에 제약이 많았음.
- 기능(팔로우/검색/채팅/룸 등)도 주체 개념에 묶여 있어, 각 도메인별로 별도 테이블/엔드포인트가 늘어났음.

## 목표
- 주체를 일반화한 `Subject`로 통합하여, **단일 도메인 모델**로 기능 재사용.
- **택소노미(Domain/Category)**로 도메인 확장/검색/필터 유연성 확보.
- **다국어 이름(SubjectName)**과 **표시명 정책** 도입.

## 고려한 대안
1) Artist 유지 + 스포츠/크리에이터에 별도 테이블 추가
    - :x: 중복 로직 증가, 공통 기능 분산, 유지보수 비용 상승
2) Artist를 최상위로 확장(필드 다수 Optional)
    - :warning: 스키마가 비대/희소, 무결성 관리 어려움
3) **Subject 도입 + 택소노미/다국어 이름 분리 (채택)**
    - :white_check_mark: 기능/스키마 간결, 확장성/검색성 우수

## 결정(요약)
- `Artist` 중심을 **`Subject`(도메인 일반화)**로 전환
- `Domain`/`Category` 도입, `SubjectName`로 다국어 이름 관리
- API: `/api/subjects/*`, `/api/taxonomy/*` 공개
- 데이터 이전: `artist_* → subject_*` 마이그레이션

## 결과 영향
- 코드: Controller/Service/Repo 전반 교체(artist → subject).
- 스키마: `subject`, `subject_name`, `subject_follow`, `subject_category_map`, `domain`, `category` 추가.
- API: 신규 subjects/taxonomy 명세 추가, Artist API는 점진 폐기(레거시 stub 유지 가능).
- FE: 라우팅 `/subject/:slug`로 통일, 리다이렉트 제공.

## 마이그레이션 개요
- 스키마 생성 → 도메인/카테고리 시드 → `Artist` 덤프를 기반으로 `Subject` 업서트 → `SubjectName`적재 → `artist_follow → subject_follow` 이전 → `room.artist_id → room.subject_id` 참조 전환 → 검증.
- 상세 절차/SQL: `docs/guides/migration-artist-to-subject.md`

## 리스크 & 완화
- 슬러그 충돌: `ON DUPLICATE KEY` + slug-join 매핑으로 안전 매핑. 필요 시 suffix 규칙.
- 로케일/국가 기본값 오탑재: 후속 보정 쿼리/CSV 매핑으로 덮어쓰기.
- 트래픽 중단: 배포 전 Read path를 Subject 우선으로 전환, 배치 전후 헬스체크.

## 롤백 전략
- `artist__backup`/`artist_follow__backup`/`room__backup` 테이블 활용 복원
- 코드 태그 롤백 + `room.artist_id` 복원(필요 시)
- `subject_*`는 드롭 금지(재시도 위해 유지)