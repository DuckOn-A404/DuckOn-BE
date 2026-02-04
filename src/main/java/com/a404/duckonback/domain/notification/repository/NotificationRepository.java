package com.a404.duckonback.domain.notification.repository;

import com.a404.duckonback.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {
    Page<Notification> findByUser_IdOrderByCreatedAtDesc(Long requestedById, Pageable pageable);
    long countByUser_IdAndReadAtIsNull(Long userId);
}
