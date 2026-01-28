package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.enums.PenaltyStatus;
import com.a404.duckonback.enums.PenaltyType;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
    List<Penalty> findByUser_Id(Long Id);
    List<Penalty> findByStatus(PenaltyStatus status);
    List<Penalty> findByPenaltyType(PenaltyType type);
    List<Penalty> findByUser_IdAndStatus(Long userId, PenaltyStatus status);

    @Modifying
    @Transactional
    @Query("""
    update Penalty p
    set p.status = com.a404.duckonback.enums.PenaltyStatus.EXPIRED
    where p.user.id = :userId
      and p.status = com.a404.duckonback.enums.PenaltyStatus.ACTIVE
      and p.endAt < :now
    """)
    void expireOldPenalties(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);

}
