# MIGRATION-ARTIST-TO-SUBJECT.md

> DuckOn BE – **Artist → Subject 전환** 데이터 마이그레이션 실무 가이드
> 대상: **MySQL 8.0+**, (옵션) **MongoDB**(채팅 컬렉션)

## 목차

1. 개요 / 전제
2. 사전 점검(Preflight)
3. 스키마 적용(신규 테이블)
4. 택소노미(도메인/카테고리) 시드
5. 슬러그 규칙 & 유틸 함수(fn_slugify)
6. Artist → Subject 변환(스테이징 & 업서트)
7. SubjectName(다국어 이름) 적재
8. 매핑 테이블(artist → subject) 생성
9. 팔로우 이전(artist_follow → subject_follow)
10. Room 참조키 교체(artist_id → subject_id)
11. (옵션) 카테고리 기본 매핑(예: MUSIC/KPOP)
12. (옵션) MongoDB 채팅 컬렉션/필드 전환
13. 검증 체크리스트
14. 롤백 가이드
15. 부록: 자주 나는 이슈 & 팁

---

## 1) 개요 / 전제

* 기존 **Artist** 중심 스키마/데이터를 일반화된 **Subject** 모델로 이전.
* 본 가이드는 **DDL/시드/마이그레이션 SQL**을 포함하며, **재실행 안전(idempotent)** 설계를 지향.
* 운영 반영 전 **반드시 백업**, 스테이징에서 **드라이런** 수행 권장.

---

## 2) 사전 점검(Preflight)

* MySQL 8.0+ 인지 확인(**`REGEXP_REPLACE`** 사용).
* 기존 테이블(예상):

    * `artist(artist_id, name_en, name_kr, debut_date, img_url, …)`
    * `artist_follow(user_id, artist_id, created_at)`
    * `room(room_id, creator_id, artist_id, title, img_url, created_at, …)`
* 신규 테이블/인덱스는 본문 **3) 스키마** 적용으로 생성.
* 애플리케이션 코드는 **Subject 기반**으로 이미 교체되어 있어야 함(전환 시점에 read path 우선 전환 권장).

---

## 3) 스키마 적용(신규 테이블)

레포에 포함된 **`duckon_subject_schema.sql`**을 그대로 실행한다.
(스키마에 `domain/category/subject/subject_name/subject_follow/subject_category_map` 및 `room(subject_id)` 포함)

> **주의**: 운영 DB에서는 **DDL 자동 커밋**이므로, 순서를 꼭 지켜 실행.

---

## 4) 택소노미(도메인/카테고리) 시드

레포의 **`duckon_subject_dump.sql`**에서 **DOMAIN/CATEGORY** 섹션까지만 우선 실행한다.
(해당 스크립트는 `ON DUPLICATE KEY UPDATE`를 사용하므로 재실행 안전)

> 주의: 스크립트에는 **SUBJECT 시드**도 포함되어 있음.
>
> * 만약 운영에서 기존 Artist를 **그대로 Subject로 승격**할 거라면, **SUBJECT 시드 블록은 임시로 주석 처리**하고 진행해도 됨.
> * 반대로 시드에 포함된 주체(예: BLACKPINK 등)를 그대로 쓰고 싶다면 시드 **전체를 실행**해도 된다. 아래 마이그레이션 로직이 **slug 충돌 시 기존 subject에 매핑**되도록 설계되어 있다.

---

## 5) 슬러그 규칙 & 유틸 함수(fn_slugify)

Subject의 **`slug`**는 FE 라우팅 키로 **불변**이어야 한다.
다음 규칙으로 생성한다:

* 소문자화
* 영숫자 외 문자는 하이픈으로 치환
* 중복 하이픈 압축, 양끝 하이픈 제거
* 예외 케이스(예: `(G)I-DLE` → `g-idle`) 괄호 제거 후 처리

**MySQL 함수 생성** (재실행 안전):

```sql
USE `duckon`;

DROP FUNCTION IF EXISTS fn_slugify;
DELIMITER $$
CREATE FUNCTION fn_slugify(src VARCHAR(255))
RETURNS VARCHAR(255)
DETERMINISTIC
BEGIN
  DECLARE s VARCHAR(255);
  IF src IS NULL OR TRIM(src) = '' THEN
    RETURN NULL;
  END IF;
  SET s = src;
  -- 괄호 제거/치환
  SET s = REPLACE(REPLACE(REPLACE(REPLACE(s, '(', ' '), ')', ' '), '[', ' '), ']', ' ');
  -- 언더스코어/슬래시/점 공백화
  SET s = REPLACE(REPLACE(REPLACE(s, '_', ' '), '/', ' '), '.', ' ');
  -- 소문자화
  SET s = LOWER(s);
  -- 영숫자 아닌 것들 하이픈으로
  SET s = REGEXP_REPLACE(s, '[^a-z0-9]+', '-');
  -- 앞뒤 하이픈 제거
  SET s = REGEXP_REPLACE(s, '(^-+)|(-+$)', '');
  -- 빈 문자열 방지
  IF s = '' THEN SET s = 'subject'; END IF;
  RETURN s;
END$$
DELIMITER ;
```

---

## 6) Artist → Subject 변환 (스테이징 & 업서트)

### 6.1 백업(강력 권장)

```sql
-- 원본 보존
CREATE TABLE IF NOT EXISTS artist__backup LIKE artist;
INSERT IGNORE INTO artist__backup SELECT * FROM artist;

CREATE TABLE IF NOT EXISTS artist_follow__backup LIKE artist_follow;
INSERT IGNORE INTO artist_follow__backup SELECT * FROM artist_follow;

CREATE TABLE IF NOT EXISTS room__backup LIKE room;
INSERT IGNORE INTO room__backup SELECT * FROM room;
```

### 6.2 스테이징 테이블 준비

* Artist에서 Subject로 옮길 **핵심 필드 준비 + slug 계산**
* `native_locale`/`country_code`는 휴리스틱 기본값 제공 (K-POP 중심 환경 가정)

```sql
DROP TABLE IF EXISTS st_subject_seed;
CREATE TABLE st_subject_seed AS
SELECT
    a.artist_id,
    -- 기본 도메인은 MUSIC (필요 시 조정)
    (SELECT d.domain_id FROM domain d WHERE d.code='MUSIC' LIMIT 1)     AS domain_id,
    NULL                                                                AS primary_category_id,
    -- 휴리스틱: 한글 이름이 있으면 ko-KR/KR, 아니면 en-US/US
    CASE WHEN a.name_kr IS NOT NULL AND TRIM(a.name_kr) <> '' THEN 'ko-KR' ELSE 'en-US' END AS native_locale,
    CASE WHEN a.name_kr IS NOT NULL AND TRIM(a.name_kr) <> '' THEN 'KR'    ELSE 'US'    END AS country_code,
    a.debut_date,
    a.img_url,
    -- 우선순위: name_en → name_kr
    CASE
      WHEN a.name_en IS NOT NULL AND TRIM(a.name_en) <> '' THEN fn_slugify(a.name_en)
      WHEN a.name_kr IS NOT NULL AND TRIM(a.name_kr) <> '' THEN fn_slugify(a.name_kr)
      ELSE CONCAT('artist-', a.artist_id)
    END AS slug
FROM artist a;
```

### 6.3 Subject 업서트(슬러그 유니크 기반)

* **중복(slug 충돌)** 시 기존 `subject` 레코드를 사용하도록 `ON DUPLICATE KEY` 사용
* 이후 매핑은 **slug 조인**으로 안전하게 복구

```sql
INSERT INTO subject
  (country_code, debut_date, created_at, domain_id, primary_category_id, native_locale, slug, img_url)
SELECT
  s.country_code, s.debut_date, NOW(6), s.domain_id, s.primary_category_id, s.native_locale, s.slug, s.img_url
FROM st_subject_seed s
ON DUPLICATE KEY UPDATE subject_id = LAST_INSERT_ID(subject_id);
-- 주의: 다중 행 삽입에서 LAST_INSERT_ID()는 마지막 1건만 반환하므로, 매핑은 slug join으로 별도 생성
```

---

## 7) SubjectName(다국어 이름) 적재

* Artist의 `name_kr`, `name_en`을 **OFFICIAL**로 적재
* 유니크 제약(`subject_id, locale_tag, name`)에 대비해 **INSERT IGNORE** 사용(중복 안전)

```sql
-- slug ↔ subject_id 매핑용 임시 뷰/테이블
DROP TABLE IF EXISTS st_subject_map_by_slug;
CREATE TABLE st_subject_map_by_slug AS
SELECT s.artist_id, subj.subject_id, subj.slug
FROM st_subject_seed s
JOIN subject subj ON subj.slug = s.slug;

-- 한국어 이름 → OFFICIAL(ko-KR)
INSERT IGNORE INTO subject_name
  (is_primary, priority, created_at, subject_id, updated_at, locale_tag, name, name_type)
SELECT
  b'1', 1, NOW(6), m.subject_id, NULL, 'ko-KR', a.name_kr, 'OFFICIAL'
FROM artist a
JOIN st_subject_map_by_slug m ON m.artist_id = a.artist_id
WHERE a.name_kr IS NOT NULL AND TRIM(a.name_kr) <> '';

-- 영어 이름 → OFFICIAL(en-US)
INSERT IGNORE INTO subject_name
  (is_primary, priority, created_at, subject_id, updated_at, locale_tag, name, name_type)
SELECT
  b'1', 1, NOW(6), m.subject_id, NULL, 'en-US', a.name_en, 'OFFICIAL'
FROM artist a
JOIN st_subject_map_by_slug m ON m.artist_id = a.artist_id
WHERE a.name_en IS NOT NULL AND TRIM(a.name_en) <> '';
```

> 필요하면 `TRANSLATED`, `ROMANIZED` 등도 추가 적재 가능.

---

## 8) 매핑 테이블(artist → subject) 생성

* 후속 마이그레이션(팔로우/룸 참조)에 사용
* 운영 추적/롤백 대비 **영구 테이블**로 유지 권장

```sql
DROP TABLE IF EXISTS artist_subject_map;
CREATE TABLE artist_subject_map (
  artist_id  BIGINT NOT NULL PRIMARY KEY,
  subject_id BIGINT NOT NULL,
  slug       VARCHAR(120) NOT NULL,
  CONSTRAINT fk_asm_subject FOREIGN KEY(subject_id) REFERENCES subject(subject_id)
);

INSERT INTO artist_subject_map(artist_id, subject_id, slug)
SELECT artist_id, subject_id, slug FROM st_subject_map_by_slug;
```

---

## 9) 팔로우 이전 (artist_follow → subject_follow)

```sql
-- 중복/재실행 안전을 위해 IGNORE
INSERT IGNORE INTO subject_follow (created_at, subject_id, user_id)
SELECT af.created_at, asm.subject_id, af.user_id
FROM artist_follow af
JOIN artist_subject_map asm ON asm.artist_id = af.artist_id;
```

---

## 10) Room 참조키 교체 (artist_id → subject_id)

### 10.1 컬럼 추가(만약 기존 room에 아직 subject_id가 없다면)

> 최신 스키마로 재생성했다면 이미 `subject_id`가 있고 `artist_id`가 제거되어 있을 수 있음.
> 아래 단계는 **기존 테이블 그대로 쓰는 경우**에만 수행.

```sql
-- 없으면 추가
ALTER TABLE room ADD COLUMN subject_id BIGINT NULL;

-- 데이터 채우기
UPDATE room r
JOIN artist_subject_map asm ON asm.artist_id = r.artist_id
SET r.subject_id = asm.subject_id
WHERE r.subject_id IS NULL;

-- FK 생성 (이전에 존재할 수 있으므로 존재 여부 확인이 필요)
ALTER TABLE room
  ADD CONSTRAINT fk_room_subject
  FOREIGN KEY (subject_id) REFERENCES subject(subject_id);

-- NOT NULL 전환
ALTER TABLE room MODIFY subject_id BIGINT NOT NULL;
```

### 10.2 artist_id 컬럼/제약 제거

**제약 이름**을 알아야 한다. (환경마다 이름 다를 수 있음)

```sql
-- 제약 이름 확인
SELECT CONSTRAINT_NAME
FROM INFORMATION_SCHEMA.KEY_COLUMN_USAGE
WHERE TABLE_SCHEMA = DATABASE()
  AND TABLE_NAME   = 'room'
  AND COLUMN_NAME  = 'artist_id'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 예: CONSTRAINT_NAME이 'FK_room_artist'라고 가정
ALTER TABLE room DROP FOREIGN KEY FK_room_artist;

-- 인덱스가 따로 있으면 삭제
SHOW INDEX FROM room WHERE Column_name='artist_id';

-- 컬럼 삭제
ALTER TABLE room DROP COLUMN artist_id;
```

---

## 11) (옵션) 카테고리 기본 매핑(예: MUSIC/KPOP)

* 모든 이전된 Subject를 **도메인 MUSIC** + **대표 카테고리 KPOP**으로 지정하고 싶다면:

```sql
-- KPOP 카테고리 id 얻기
SET @cat_kpop := (
  SELECT c.category_id
  FROM category c JOIN domain d ON d.domain_id = c.domain_id
  WHERE d.code='MUSIC' AND c.code='KPOP'
  LIMIT 1
);

-- 대표 카테고리 설정 (비어있는 경우에만)
UPDATE subject s
SET s.primary_category_id = @cat_kpop
WHERE s.primary_category_id IS NULL;

-- subject_category_map에 KPOP 매핑(없으면 삽입)
INSERT IGNORE INTO subject_category_map(category_id, subject_id)
SELECT @cat_kpop, s.subject_id
FROM subject s;
```

> 그룹/솔로/성별 등 추가 분류는 별도 기준(메타데이터)으로 후속 배치에서 채우는 것을 권장.

---

## 12) (옵션) MongoDB 채팅 컬렉션/필드 전환

> 기존 컬렉션: `artist_chats` (필드: `artistId`)
> 신규 컬렉션: `subject_chats` (필드: `subjectId`)

```javascript
// mongo shell 혹은 mongosh

use duckon;

// 1) 컬렉션 이름 변경 (없으면 스킵)
if (db.getCollectionNames().includes('artist_chats') && !db.getCollectionNames().includes('subject_chats')) {
  db.artist_chats.renameCollection('subject_chats');
}

// 2) 필드 이름 변경 artistId -> subjectId
db.subject_chats.updateMany(
  { artistId: { $exists: true } },
  [{ $set: { subjectId: "$artistId" } }, { $unset: "artistId" }]
);

// 3) subjectId 타입 일치화(문자열 선호 시)
db.subject_chats.updateMany(
  { subjectId: { $exists: true, $not: { $type: "string" } } },
  [{ $set: { subjectId: { $toString: "$subjectId" } } }]
);
```

---

## 13) 검증 체크리스트

```sql
-- A. 매핑 개수 확인 (artist ↔ subject)
SELECT COUNT(*) AS artist_cnt FROM artist;
SELECT COUNT(DISTINCT artist_id) AS mapped_artist_cnt FROM artist_subject_map;

-- B. subject 행 존재 여부(최소 artist 수 이상이어야 함; 시드 포함 시 더 클 수 있음)
SELECT COUNT(*) AS subject_cnt FROM subject;

-- C. 팔로우 이전 (건수 일치)
SELECT COUNT(*) AS artist_follow_cnt FROM artist_follow;
SELECT COUNT(*) AS subject_follow_cnt FROM subject_follow;

-- D. room 참조 전환
SELECT COUNT(*) AS null_subject_in_room FROM room WHERE subject_id IS NULL;

-- E. SubjectName 다국어 등록
SELECT COUNT(*) FROM subject_name WHERE locale_tag='ko-KR';
SELECT COUNT(*) FROM subject_name WHERE locale_tag='en-US';

-- F. 대표 카테고리/맵핑(옵션 수행 시)
SELECT COUNT(*) FROM subject WHERE primary_category_id IS NOT NULL;
SELECT COUNT(*) FROM subject_category_map;
```

**수작업 확인**

* FE 라우팅: `/subject/:slug`로 상세 정상 접근
* 검색/정렬/팔로우/카테고리 필터 API 정상
* 브라우저 302 리다이렉트(`/api/chat/subject/{id}/message` → `/subject/{slug}`) 동작

---

## 14) 롤백 가이드

> 롤백은 **코드/데이터** 두 축을 함께 고려.

* **코드**: 이전 태그/커밋으로 되돌림.
* **데이터**

    * `artist__backup`, `artist_follow__backup`, `room__backup`에서 복구
    * `room`에 `artist_id` 컬럼/제약 복원 (필요 시 백업 대비)
    * `subject_*` 테이블은 **드롭 금지** 권장(재시도 쉽게 하기 위해 유지).
      필요하면 `TRUNCATE subject_follow/subject_name/subject_category_map` 등만 수행.
* **Mongo**: 반대로 `subject_chats → artist_chats` rename, `subjectId → artistId` 필드 복원.

---

## 15) 부록: 자주 나는 이슈 & 팁

* **slug 중복**

    * 시드가 먼저 들어가 있거나, 다른 Artist끼리 충돌할 수 있음.
    * 본 문서는 `ON DUPLICATE KEY` + **slug 조인 매핑**으로 안전하게 기존 레코드로 매핑한다.
    * 더 강하게 중복 회피하려면 `fn_slugify` 결과가 중복일 때 `-<artist_id>` suffix를 붙이는 배치를 별도 수행:

      ```sql
      UPDATE st_subject_seed s
      JOIN (
        SELECT slug FROM st_subject_seed GROUP BY slug HAVING COUNT(*) > 1
      ) dup ON dup.slug = s.slug
      SET s.slug = CONCAT(s.slug, '-', s.artist_id);
      ```
* **국가/로케일 기본값**

    * 본문은 `ko-KR/KR` ↔ `en-US/US` 간 단순 휴리스틱.
    * 필요 시 **CSV 매핑 테이블**을 만들어 UPDATE로 정확히 덮어쓰길 권장.
* **트랜잭션**

    * DDL은 자동 커밋. 데이터 이전(INSERT/UPDATE)은 묶어도 되지만, 대량 적재면 **배치 단위 커밋**이 안전.
* **성능**

    * FK 체크를 끄고 적재 후 켜면 빠르지만, 운영에서는 FK 유지가 더 안전.
    * 본 문서는 FK ON 기준으로 작성.

---

## 전체 실행 순서 (요약 체크리스트)

1. **백업**: `artist__backup`, `artist_follow__backup`, `room__backup`
2. **스키마 적용**: `duckon_subject_schema.sql`
3. **택소노미 시드**: `duckon_subject_dump.sql` (도메인/카테고리만 우선)
4. **함수 생성**: `fn_slugify`
5. **스테이징 생성**: `st_subject_seed`
6. **Subject 업서트**
7. **slug 기반 매핑 테이블 생성**: `st_subject_map_by_slug` → `artist_subject_map`
8. **SubjectName 적재(ko/en, OFFICIAL)**
9. **팔로우 이전**: `artist_follow → subject_follow`
10. **Room 참조 전환**: `subject_id` 채우고 FK/NOT NULL, `artist_id` 제거
11. **(옵션)** KPOP 등 기본 카테고리 매핑
12. **(옵션)** Mongo 컬렉션/필드 전환
13. **검증 쿼리** 전부 통과 확인
14. **애플리케이션 배포** & 최종 점검

