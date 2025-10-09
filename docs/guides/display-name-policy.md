# 표시명(Display Name) 정책

목표: **요청자/문서의 표시 언어**에 맞춰 Subject의 대표 이름을 일관되게 제공.

## 용어
- `displayLocale`: 표시 대상 로케일 (예: `ko`, `en`, `ja`, `en-US` 등)
- `nativeLocale`: Subject의 원어 로케일 (`subject.native_locale`)
- `SubjectName`: (subject_id, locale_tag, name, name_type, primary, priority)

## 결정 규칙(우선순위)
1) **표시 로케일 Official 1건**
    - `locale_tag = displayLocale`, `name_type = OFFICIAL`
    - 정렬: `PRIMARY DESC`, `PRIORITY ASC` → 첫 1건
2) **원어 로케일 Official 1건**
    - `locale_tag = nativeLocale`, `name_type = OFFICIAL`
3) **표시 로케일의 ROMANIZED/TRANSLATED/ALIAS 1건**
    - 타입 우선순위 권장: `ROMANIZED > TRANSLATED > ALIAS`
4) **아무 로케일 Official 1건**
5) **Fallback**: `slug`

> 위 로직은 Repository 레벨에서 SQL로 구현되어 있으며, 목록 API는 `displayLocale` 파라미터(내부)로 계산한다.  
> Controller는 사용자의 선호 언어(예: JWT 내 `user.language` 또는 `Accept-Language`)를 주입해 호출하는 것을 권장.

## 예시
- 요청 로케일: `ko`
    - `(G)I-DLE` → `아이들`(OFFICIAL, ko-KR 존재)
    - `wave to earth` → `웨이브 투 어스`(TRANSLATED, ko-KR), 없으면 `wave to earth`(OFFICIAL, en-US)

## 추가 규칙
- **정규화**: `displayLocale` 비교 시 소문자/하이픈 케이스 통일(예: `ko-KR` ↔ `ko`)
- **중복 처리**: 동일 이름 중복 INSERT 방지: `UNIQUE(subject_id, locale_tag, name)`
- **성능 팁**: 목록 조회는 `MAX(CASE WHEN ...)` 패턴으로 1-pass 계산 (현재 구현과 동일).
