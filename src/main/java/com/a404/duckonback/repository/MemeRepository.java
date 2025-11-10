package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Meme;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface MemeRepository extends JpaRepository<Meme, Long> {
}
