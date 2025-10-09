# 스키마 요약 — 테이블/인덱스/제약

## domain
- PK: `domain_id`
- 컬럼: `code`(UK), `name`
- UK: `(code)`
- 예: `MUSIC`, `SPORTS`, `CREATOR`

## category
- PK: `category_id`
- FK: (`domain_id`) → domain, (`parent_id`) → category (self tree)
- UK: `(domain_id, code)`
- 컬럼: `depth`(0=root), `code`, `name`

## subject
- PK: `subject_id`
- FK: `domain_id` → domain, `primary_category_id` → category
- UK: `(slug)`
- 컬럼: `slug`(<=120), `native_locale`, `country_code`, `debut_date`, `img_url`, `created_at`
- IDX: `idx_subject_domain(domain_id)`, `idx_subject_primary_cat(primary_category_id)`

## subject_name
- PK: `name_id`
- FK: `subject_id` → subject
- UK: `(subject_id, locale_tag, name)`
- 컬럼: `locale_tag`(e.g. `ko-KR`), `name`, `name_type(ENUM)`, `is_primary(BOOL)`, `priority(SMALLINT)`, `created_at`, `updated_at`
- IDX: `idx_sn_subject`, `idx_sn_locale`, `idx_sn_name`

## subject_category_map
- PK: `(category_id, subject_id)`
- FK: `category_id` → category, `subject_id` → subject

## subject_follow
- PK: `(subject_id, user_id)`
- FK: `subject_id` → subject, `user_id` → user
- 컬럼: `created_at`
- 파생: 팔로워 수 집계에 사용

## room
- PK: `room_id`
- FK: `creator_id` → user, `subject_id` → subject
- 컬럼: `title`, `img_url`, `created_at`

## user (요약)
- PK: `id`
- UK: `(user_id)`, `(email)`, `(provider, provider_id)`
- 컬럼: `language`, `img_url`, `deleted(BOOL)`, `deleted_at`, `role`, ...
- 연관: `subject_follow`(N:M), `room`(1:N)

---

## 관계(ER 개요)
- `domain (1) ──< category (N)` (self-parent로 트리)
- `domain (1) ──< subject (N)`
- `category (N) ──< subject_category_map >── (N) subject`
- `subject (1) ──< subject_name (N)`
- `subject (N) ──< subject_follow (N) >── (N) user`
- `subject (1) ──< room (N)`, `user (1) ──< room (N)`

> ERD 이미지는 `schema/erd.png`로 관리. 아래 머메이드 다이어그램을 png로 변환해도 됨.

### Mermaid (생성용)
```mermaid
erDiagram
  DOMAIN ||--o{ CATEGORY : has
  CATEGORY ||--o{ CATEGORY : parent
  DOMAIN ||--o{ SUBJECT : has
  SUBJECT ||--o{ SUBJECT_NAME : has
  SUBJECT ||--o{ SUBJECT_CATEGORY_MAP : in
  CATEGORY ||--o{ SUBJECT_CATEGORY_MAP : maps
  SUBJECT ||--o{ SUBJECT_FOLLOW : has
  USER ||--o{ SUBJECT_FOLLOW : does
  USER ||--o{ ROOM : creates
  SUBJECT ||--o{ ROOM : themed

  DOMAIN { bigint domain_id PK
           string code UK
           string name }
  CATEGORY { bigint category_id PK
             bigint domain_id FK
             bigint parent_id FK
             tinyint depth
             string code
             string name }
  SUBJECT { bigint subject_id PK
            bigint domain_id FK
            bigint primary_category_id FK
            string native_locale
            string country_code
            string slug UK
            date debut_date
            text img_url
            datetime created_at }
  SUBJECT_NAME { bigint name_id PK
                 bigint subject_id FK
                 string locale_tag
                 string name
                 enum name_type
                 bool is_primary
                 smallint priority
                 datetime created_at
                 datetime updated_at }
  SUBJECT_CATEGORY_MAP { bigint category_id PK,FK
                         bigint subject_id PK,FK }
  SUBJECT_FOLLOW { bigint subject_id PK,FK
                   bigint user_id PK,FK
                   datetime created_at }
  ROOM { bigint room_id PK
         bigint creator_id FK
         bigint subject_id FK
         string title
         text img_url
         datetime created_at }
  USER { bigint id PK
         string user_id UK
         string email UK
         string language
         bool deleted
         datetime deleted_at
         string img_url
         enum role }
````

---

## 성능/인덱스 팁

* 목록 조회: `subject_follow`는 COUNT DISTINCT가 아닌 `LEFT JOIN + GROUP BY`로 집계(현 구현).
* 검색: `subject_name.name` LIKE 인덱스 타기 어려움 → prefix 검색/서브셋 캐시 고려.
* 조인 순서: 페이지 쿼리는 `subject` 주도 후 이름 선택(`MAX(CASE WHEN ...)`)으로 1-pass.
