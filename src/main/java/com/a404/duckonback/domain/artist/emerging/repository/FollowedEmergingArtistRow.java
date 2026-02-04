package com.a404.duckonback.domain.artist.emerging.repository;

import com.a404.duckonback.domain.artist.emerging.entity.EmergingArtistStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface FollowedEmergingArtistRow {
    Long getEmergingArtistId();
    LocalDateTime getCreatedAt();   // follow.createdAt
    LocalDate getDebutDate();
    String getNameKr();
    String getNameEn();
    String getImgUrl();
    EmergingArtistStatus getStatus();
    Long getFollowerCount();
}
