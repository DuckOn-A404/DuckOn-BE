package com.a404.duckonback.repository;

import com.a404.duckonback.dto.ArtistDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class ArtistRepositoryImpl implements ArtistRepositoryCustom {

    private final EntityManager em;

    // ===== 초성 유틸 =====
    private static final int HANGUL_BASE = 0xAC00; // '가'
    private static final int CHOSEONG_INTERVAL = 21 * 28; // 588
    private static final Map<Character, Integer> INITIAL_INDEX = Map.ofEntries(
            Map.entry('ㄱ', 0), Map.entry('ㄲ', 1), Map.entry('ㄴ', 2), Map.entry('ㄷ', 3),
            Map.entry('ㄸ', 4), Map.entry('ㄹ', 5), Map.entry('ㅁ', 6), Map.entry('ㅂ', 7),
            Map.entry('ㅃ', 8), Map.entry('ㅅ', 9), Map.entry('ㅆ', 10), Map.entry('ㅇ', 11),
            Map.entry('ㅈ', 12), Map.entry('ㅉ', 13), Map.entry('ㅊ', 14), Map.entry('ㅋ', 15),
            Map.entry('ㅌ', 16), Map.entry('ㅍ', 17), Map.entry('ㅎ', 18)
    );

    private static boolean isInitialChar(char ch) {
        return INITIAL_INDEX.containsKey(ch);
    }

    private static String initialStartChar(char initial) {
        int idx = INITIAL_INDEX.get(initial);
        char c = (char) (HANGUL_BASE + idx * CHOSEONG_INTERVAL);
        return String.valueOf(c);
    }

    private static String initialEndChar(char initial) {
        int idx = INITIAL_INDEX.get(initial);
        char c = (char) (HANGUL_BASE + (idx + 1) * CHOSEONG_INTERVAL - 1);
        return String.valueOf(c);
    }

    @Override
    public Page<ArtistDTO> pageArtists(Pageable pageable, String sort, String order, String keyword) {

        String orderDir = "asc".equalsIgnoreCase(order) ? "ASC" : "DESC";

        // ----- WHERE 절 구성 -----
        String where = "";
        boolean useKwLike = false;
        String kwLike = null;

        String prefix = null;
        Character initial = null;

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();

            // (a) 초성만 1글자 → 해당 초성으로 시작
            if (kw.length() == 1 && isInitialChar(kw.charAt(0))) {
                initial = kw.charAt(0);
                where = " WHERE a.name_kr >= :krStart AND a.name_kr <= :krEnd ";
            }
            // (b) 접두 + 초성 (예: 빅ㅂ)
            else if (kw.length() >= 2) {
                char last = kw.charAt(kw.length() - 1);
                if (isInitialChar(last)) {
                    initial = last;
                    prefix = kw.substring(0, kw.length() - 1);

                    where = """
                        WHERE (
                            LOCATE(:prefix, a.name_kr) > 0 AND
                            SUBSTRING(
                                a.name_kr,
                                LOCATE(:prefix, a.name_kr) + CHAR_LENGTH(:prefix),
                                1
                            ) BETWEEN :krStart AND :krEnd
                        )
                        """;
                } else {
                    // 일반 키워드
                    useKwLike = true;
                    kwLike = "%" + kw.toLowerCase() + "%";
                    where = " WHERE LOWER(a.name_kr) LIKE :kw OR LOWER(a.name_en) LIKE :kw ";
                }
            } else {
                // 일반 키워드(단일 글자)
                useKwLike = true;
                kwLike = "%" + kw.toLowerCase() + "%";
                where = " WHERE LOWER(a.name_kr) LIKE :kw OR LOWER(a.name_en) LIKE :kw ";
            }
        }

        // ----- ORDER BY -----
        String orderBy;
        switch ((sort == null ? "followers" : sort).toLowerCase()) {
            case "name" -> orderBy = " a.name_kr " + orderDir + ", a.name_en " + orderDir + ", a.artist_id ASC ";
            case "debut" -> orderBy = " a.debut_date " + orderDir + ", a.artist_id ASC ";
            default -> orderBy = " follower_count " + orderDir + ", a.artist_id ASC ";
        }

        // 메인 쿼리
        String selectSql = """
            SELECT a.artist_id, a.name_en, a.name_kr, a.debut_date, a.img_url,
                   COALESCE(COUNT(af.user_id), 0) AS follower_count
            FROM artist a
            LEFT JOIN artist_follow af ON af.artist_id = a.artist_id
            """ + where + """
            GROUP BY a.artist_id, a.name_en, a.name_kr, a.debut_date, a.img_url
            ORDER BY """ + orderBy + """
            LIMIT :limit OFFSET :offset
        """;

        // 카운트 쿼리 (JOIN 불필요)
        String countSql = """
            SELECT COUNT(*)
            FROM artist a
        """ + (where.isBlank() ? "" : " " + where);

        Query selectQ = em.createNativeQuery(selectSql);
        Query countQ  = em.createNativeQuery(countSql);

        // 파라미터 바인딩
        if (keyword != null && !keyword.isBlank()) {
            if (initial != null && prefix == null) {
                // (a) 초성만
                selectQ.setParameter("krStart", initialStartChar(initial));
                selectQ.setParameter("krEnd",   initialEndChar(initial));
                countQ.setParameter("krStart",  initialStartChar(initial));
                countQ.setParameter("krEnd",    initialEndChar(initial));
            } else if (initial != null && prefix != null) {
                // (b) 접두 + 초성
                selectQ.setParameter("prefix", prefix);
                selectQ.setParameter("krStart", initialStartChar(initial));
                selectQ.setParameter("krEnd",   initialEndChar(initial));

                countQ.setParameter("prefix", prefix);
                countQ.setParameter("krStart", initialStartChar(initial));
                countQ.setParameter("krEnd",   initialEndChar(initial));
            } else if (useKwLike) {
                selectQ.setParameter("kw", kwLike);
                countQ.setParameter("kw", kwLike);
            }
        }

        selectQ.setParameter("limit", pageable.getPageSize());
        selectQ.setParameter("offset", (int) pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = selectQ.getResultList();
        long total = ((Number) countQ.getSingleResult()).longValue(); // ← (중복 선언 제거)

        List<ArtistDTO> content = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long artistId = ((Number) r[0]).longValue();
            String nameEn = (String) r[1];
            String nameKr = (String) r[2];
            LocalDate debutDate = (r[3] != null) ? ((Date) r[3]).toLocalDate() : null;
            String imgUrl = (String) r[4];
            long followerCount = ((Number) r[5]).longValue();

            content.add(ArtistDTO.builder()
                    .artistId(artistId)
                    .nameEn(nameEn)
                    .nameKr(nameKr)
                    .debutDate(debutDate)
                    .imgUrl(imgUrl)
                    .followerCount(followerCount)
                    .build());
        }

        return new PageImpl<>(content, pageable, total);
    }
}
