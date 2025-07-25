package com.a404.duckonback.repository;

import com.a404.duckonback.entity.UserBlock;
import com.a404.duckonback.entity.UserBlockId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserBlockRepository extends JpaRepository<UserBlock, UserBlockId> {
    List<UserBlock> findByBlocker_Id(Long blockerId);
    List<UserBlock> findByBlocked_Id(Long blockedId);
    boolean existsByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);
    void deleteByBlocker_IdAndBlocked_Id(Long blockerId, Long blockedId);
}
