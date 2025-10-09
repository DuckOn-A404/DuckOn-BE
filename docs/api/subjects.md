# Subjects API

Base URL: `/api/subjects`

공통 응답 에러 포맷:
```json
{ "message": "<설명>" }
````

## 1) 목록/검색/정렬

```
GET /api/subjects
```

### Query

* `page` (int, default 1)
* `size` (int, default 30)
* `sort` (string, default `followers`) — `followers|name|debut`
* `order` (string, default `desc`) — `asc|desc`
* `keyword` (string, optional) — 다국어 이름 LIKE 검색

> 표시명은 [표시명 정책](../guides/display-name-policy.md)에 따라 계산됨.

### Response 200

```json
{
  "subjectList": [
    {
      "subjectId": 123,
      "slug": "blackpink",
      "displayName": "블랙핑크",
      "debutDate": "2016-08-08",
      "imgUrl": null,
      "followerCount": 9999
    }
  ],
  "page": 1,
  "size": 30,
  "totalPages": 10,
  "totalElements": 300
}
```

### 예시

```bash
curl '/api/subjects?page=1&size=20&sort=name&order=asc&keyword=뉴진스'
```

---

## 2) 상세 조회 (택소노미 옵션)

```
GET /api/subjects/{subjectId}
```

### Query

* `includeTaxonomy` (bool, default `false`)
  `true`면 도메인/대표카테고리/전체카테고리 포함

### Response 200

```json
{
  "subjectId": 123,
  "slug": "blackpink",
  "displayName": "블랙핑크",
  "nativeLocale": "ko-KR",
  "countryCode": "KR",
  "debutDate": "2016-08-08",
  "imgUrl": null,
  "followed": true,
  "followedAt": "2025-10-09T12:34:56",
  "domainCode": "MUSIC",
  "primaryCategory": { "id": 7, "code": "KPOP", "name": "케이팝" },
  "categories": [
    { "id": 7, "code": "KPOP", "name": "케이팝" },
    { "id": 15, "code": "FEMALE", "name": "여성" }
  ]
}
```

---

## 3) 카테고리 기반 검색

```
GET /api/subjects/category
```

### Query

* `domain` (string, optional) — 예: `MUSIC|SPORTS|CREATOR`
* `codes` (string, optional) — 콤마로 구분된 카테고리 코드 목록 (예: `KPOP,FEMALE`)
* `match` (string, default `any`) — `any|all` (모두 포함/하나 이상 포함)
* `page` (int, default 1), `size` (int, default 30)

### Response 200

목록 API와 동일한 페이징 페이로드.

### 예시

```bash
curl '/api/subjects/category?domain=SPORTS&codes=FOOTBALL,PLAYER&match=all&page=1&size=24'
```

---

## 4) 키워드 단건 검색(리스트, 비페이징)

```
GET /api/subjects?keyword=<kw>
```

* `page/size/sort/order` 파라미터 없이 `keyword`만 있으면 단순 리스트 반환:

```json
{ "subjectList": [ { "subjectId": 1, "slug": "...", "displayName": "...", ... } ] }
```

---

## 5) 랜덤

```
GET /api/subjects/random?size=16
```

### Response 200

```json
{ "subjectList": [ ... ] }
```

---

## 6) 내가 팔로우한 주체(인증 필요)

```
GET /api/subjects/me?page=1&size=10
Authorization: Bearer <JWT>
```

### Response 200

```json
{
  "subjectList": [
    {
      "subjectId": 123,
      "slug": "blackpink",
      "displayName": "블랙핑크",
      "debutDate": "2016-08-08",
      "imgUrl": null
    }
  ],
  "page": 1, "size": 10, "totalPages": 3, "totalElements": 30
}
```

---

## 7) 팔로우/언팔로우/일괄수정 (인증 필요)

### 팔로우

```
POST /api/subjects/{subjectId}/follow
Authorization: Bearer <JWT>
```

* `201 Created`

```json
{ "message": "대상을 팔로우했습니다." }
```

### 언팔로우

```
DELETE /api/subjects/{subjectId}/follow
Authorization: Bearer <JWT>
```

* `200 OK`

```json
{ "message": "대상 팔로우를 취소했습니다." }
```

### 일괄 수정

```
PUT /api/subjects/follow
Authorization: Bearer <JWT>
Content-Type: application/json

{ "subjectList": [1,2,3] }
```

* `201 Created`

```json
{ "message": "Subject 팔로우 목록을 수정했습니다." }
```

---

## 상태코드 요약

* `200 OK`, `201 Created`
* `400 Bad Request` 잘못된 파라미터
* `401 Unauthorized` JWT 누락/만료
* `403 Forbidden` 권한 부족
* `404 Not Found` 없는 subjectId

