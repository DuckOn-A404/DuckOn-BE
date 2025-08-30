package com.a404.duckonback.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name="category",
    uniqueConstraints=@UniqueConstraint(name="uk_category_domain_code", columnNames={"domain_id","code"}))
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long categoryId;

    @ManyToOne(fetch= FetchType.LAZY) @JoinColumn(name="domain_id", nullable=false)
    private Domain domain;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="parent_id")
    private Category parent;

    @Column(nullable=false, length=100) private String code;
    @Column(nullable=false, length=100) private String name;
    private byte depth;
}

