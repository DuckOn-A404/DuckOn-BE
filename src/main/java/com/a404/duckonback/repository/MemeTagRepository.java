package com.a404.duckonback.repository;

import com.a404.duckonback.entity.MemeTag;
import com.a404.duckonback.entity.MemeTagId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemeTagRepository extends JpaRepository<MemeTag, MemeTagId> {
}
