package com.a404.duckonback.repository;

import com.a404.duckonback.dto.SubjectDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class SubjectRepositoryImpl implements SubjectRepositoryCustom {

    private final EntityManager em;

    @Override
    public Page<SubjectDTO> pageSubjects(Pageable pageable,
        String sort,
        String order,
        String keyword,
        String displayLocale) {

        String orderDir = "asc".equalsIgnoreCase(order) ? "ASC" : "DESC";

        // --- 검색(where) 구성: 다국어 이름 테이블에서 LIKE
        boolean useKw = keyword != null && !keyword.isBlank();
        String where = useKw ? " WHERE LOWER(sn.name) LIKE :kw " : "";

        // --- 정렬 기준
        String orderBy;
        switch ((sort == null ? "followers" : sort).toLowerCase()) {
            case "name"  -> orderBy = " display_name " + orderDir + ", s.subject_id ASC ";
            case "debut" -> orderBy = " s.debut_date " + orderDir + ", s.subject_id ASC ";
            default      -> orderBy = " follower_count " + orderDir + ", s.subject_id ASC ";
        }

        // --- 메인 쿼리
        String selectSql = """
            SELECT
              s.subject_id,
              s.slug,
              s.debut_date,
              s.img_url,
              COALESCE(
                MAX(CASE WHEN sn2.locale_tag = :disp AND sn2.name_type = 'OFFICIAL' THEN sn2.name END),
                MAX(CASE WHEN sn2.locale_tag = s.native_locale AND sn2.name_type = 'OFFICIAL' THEN sn2.name END)
              ) AS display_name,
              COALESCE(COUNT(DISTINCT sf.user_id), 0) AS follower_count
            FROM subject s
            LEFT JOIN subject_follow sf ON sf.subject_id = s.subject_id
            LEFT JOIN subject_name sn2   ON sn2.subject_id = s.subject_id
            """ + (useKw ? " JOIN subject_name sn ON sn.subject_id = s.subject_id " : "") + where + """
            GROUP BY s.subject_id, s.slug, s.debut_date, s.img_url
            ORDER BY """ + orderBy + """
            LIMIT :limit OFFSET :offset
        """;

        // --- 카운트 쿼리
        String countSql = """
            SELECT COUNT(DISTINCT s.subject_id)
            FROM subject s
            """ + (useKw ? " JOIN subject_name sn ON sn.subject_id = s.subject_id " : "") + where;

        Query selectQ = em.createNativeQuery(selectSql);
        Query countQ  = em.createNativeQuery(countSql);

        String disp = (displayLocale == null || displayLocale.isBlank()) ? "ko" : displayLocale.toLowerCase();
        selectQ.setParameter("disp", disp);

        if (useKw) {
            String kwLike = "%" + keyword.trim().toLowerCase() + "%";
            selectQ.setParameter("kw", kwLike);
            countQ.setParameter("kw", kwLike);
        }

        selectQ.setParameter("limit", pageable.getPageSize());
        selectQ.setParameter("offset", (int) pageable.getOffset());

        @SuppressWarnings("unchecked")
        List<Object[]> rows = selectQ.getResultList();
        long total = ((Number) countQ.getSingleResult()).longValue();

        List<SubjectDTO> content = new ArrayList<>(rows.size());
        for (Object[] r : rows) {
            Long       subjectId   = ((Number) r[0]).longValue();
            String     slug        = (String) r[1];
            LocalDate  debutDate   = (r[2] != null) ? ((Date) r[2]).toLocalDate() : null;
            String     imgUrl      = (String) r[3];
            String     displayName = (String) r[4];
            long       followerCnt = ((Number) r[5]).longValue();

            content.add(SubjectDTO.builder()
                .subjectId(subjectId)
                .slug(slug)
                .displayName(displayName)
                .debutDate(debutDate)
                .imgUrl(imgUrl)
                .followerCount(followerCnt)
                .build());
        }

        return new PageImpl<>(content, pageable, total);
    }
}
