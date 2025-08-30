package com.a404.duckonback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="domain")
@Getter
@Setter
public class Domain {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long domainId;
    @Column(nullable=false, unique=true, length=50) private String code;
    @Column(nullable=false, length=100) private String name;
}