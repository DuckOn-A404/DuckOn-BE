package com.a404.duckonback.repository;

import com.a404.duckonback.entity.Domain;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainRepository extends JpaRepository<Domain, Long> {}