package com.a404.duckonback.repository;

import com.a404.duckonback.entity.MemeHourlyTop10;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MemeHourlyTop10Repository extends JpaRepository<MemeHourlyTop10, Long> {

    @Query("""
        SELECT m
        FROM MemeHourlyTop10 m
        WHERE m.hourStart = (
            SELECT MAX(m2.hourStart) FROM MemeHourlyTop10 m2
        )
        ORDER BY m.rankNo ASC
    """)
    List<MemeHourlyTop10> findLatestTop10();
}
