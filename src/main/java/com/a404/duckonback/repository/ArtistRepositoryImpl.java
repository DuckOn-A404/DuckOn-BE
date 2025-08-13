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

@Repository
@RequiredArgsConstructor
public class ArtistRepositoryImpl implements ArtistRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<ArtistDTO> pageArtists(Pageable pageable, String sort, String order, String keyword) {
        // ORDER BY 컬럼 화이트리스트
        String orderDir = "asc".equalsIgnoreCase(order) ? "ASC" : "DESC";

        String where = "";
        if (keyword != null && !keyword.isBlank()) {
            where = " WHERE LOWER(a.name_en) LIKE :kw OR LOWER(a.name_kr) LIKE :kw ";
        }

        String orderBy;
        switch ((sort == null ? "followers" : sort).toLowerCase()) {
            case "name" -> orderBy = " a.name_kr " + orderDir + ", a.name_en " + orderDir + ", a.artist_id ASC ";
            case "debut" -> orderBy = " a.debut_date " + orderDir + ", a.artist_id ASC ";
            default -> orderBy = " follower_count " + orderDir + ", a.artist_id ASC ";
        }

        // 메인 쿼리 (페이지 데이터)
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

        // 전체 개수 (count 쿼리)
        String countSql = """
            SELECT COUNT(*)
            FROM artist a
        """ + (where.isBlank() ? "" : " " + where);

        Query selectQ = em.createNativeQuery(selectSql);
        Query countQ = em.createNativeQuery(countSql);

        if (!where.isBlank()) {
            String kw = "%" + keyword.toLowerCase() + "%";
            selectQ.setParameter("kw", kw);
            countQ.setParameter("kw", kw);
        }

        selectQ.setParameter("limit", pageable.getPageSize());
        selectQ.setParameter("offset", (int) pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = selectQ.getResultList();

        long total = ((Number) countQ.getSingleResult()).longValue();

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
