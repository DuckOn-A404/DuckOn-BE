package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {}