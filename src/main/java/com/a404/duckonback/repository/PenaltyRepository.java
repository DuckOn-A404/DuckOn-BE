package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Penalty;
import com.a404.duckonback.enums.PenaltyStatus;
import com.a404.duckonback.enums.PenaltyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaltyRepository extends JpaRepository<Penalty, Long> {
    List<Penalty> findByUser_Id(Long Id);
    List<Penalty> findByStatus(PenaltyStatus status);
    List<Penalty> findByPenaltyType(PenaltyType type);
}
