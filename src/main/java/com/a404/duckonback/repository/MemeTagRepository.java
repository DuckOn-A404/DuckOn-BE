package com.a404.duckonback.repository;

import com.a404.duckonback.entity.MemeTag;
import com.a404.duckonback.entity.MemeTagId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MemeTagRepository extends JpaRepository<MemeTag, MemeTagId> {
    @Query("""
        SELECT mt.meme.id, t.tagName
        FROM MemeTag mt
        JOIN mt.tag t
        WHERE mt.meme.id IN :memeIds
        """)
    List<Object[]> findTagPairsByMemeIds(@Param("memeIds") List<Long> memeIds);
}
