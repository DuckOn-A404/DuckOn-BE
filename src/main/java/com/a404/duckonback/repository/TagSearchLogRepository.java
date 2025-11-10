package com.a404.duckonback.repository;

import com.a404.duckonback.dto.TrendingTagDTO;
import com.a404.duckonback.entity.TagSearchLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TagSearchLogRepository extends JpaRepository<TagSearchLog, Long> {

    @Query("""
        select new com.a404.duckonback.dto.TrendingTagDTO(
            l.tag.id,
            l.tag.tagName,
            count(l.id)
        )
        from TagSearchLog l
        where l.searchedAt >= :from
        group by l.tag.id, l.tag.tagName
        order by count(l.id) desc
        """)
    List<TrendingTagDTO> findTrendingTags(@Param("from") LocalDateTime from, Pageable pageable);
}
