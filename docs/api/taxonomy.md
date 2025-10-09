# Taxonomy API

Base URL: `/api/taxonomy`

## 1) 도메인 목록
````

GET /api/taxonomy/domains

````
### Response 200
```json
{
  "domains": [
    { "id": 1, "code": "MUSIC", "name": "Music" },
    { "id": 2, "code": "SPORTS", "name": "Sports" },
    { "id": 3, "code": "CREATOR", "name": "Creator / Influencer" }
  ]
}
````

---

## 2) 특정 도메인의 카테고리 트리

```
GET /api/taxonomy/domains/{domainCode}/categories/tree
```

### Response 200

```json
{
  "domain": { "id": 1, "code": "MUSIC", "name": "Music" },
  "roots": [
    {
      "id": 10, "code": "GENRE", "name": "장르", "depth": 0,
      "children": [
        { "id": 11, "code": "KPOP", "name": "케이팝", "depth": 1, "children": [] },
        { "id": 12, "code": "JPOP", "name": "제이팝", "depth": 1, "children": [] }
      ]
    }
  ]
}
```

---

## 3) 특정 도메인의 카테고리(플랫)

```
GET /api/taxonomy/domains/{domainCode}/categories
```

### Response 200

```json
{
  "categories": [
    { "id": 10, "code": "GENRE", "name": "장르", "depth": 0, "parentId": null },
    { "id": 11, "code": "KPOP", "name": "케이팝", "depth": 1, "parentId": 10 }
  ]
}
```

### 상태코드

* `200 OK`, `404 Not Found`(없는 domainCode)

