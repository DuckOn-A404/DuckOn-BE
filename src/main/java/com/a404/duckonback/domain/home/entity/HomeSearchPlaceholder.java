package com.a404.duckonback.domain.home.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "home_search_placeholder")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class HomeSearchPlaceholder {
    @Id
    private Long id; // 항상 1로 고정 (Single row)

    @Column(name = "items", nullable = false, columnDefinition = "json")
    private String itemsJson; // JSON 배열 문자열: ["a","b",...]

    @Column(nullable = false)
    private Long version;

    @Column(name = "updated_by")
    private Long updatedBy;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updateItems(String newItemsJson, Long updaterId) {
        this.itemsJson = newItemsJson;
        this.updatedBy = updaterId;
        this.version = this.version + 1;
    }

    public static HomeSearchPlaceholder initDefault(Long updaterId, String itemsJson) {
        return HomeSearchPlaceholder.builder()
                .id(1L)
                .itemsJson(itemsJson)
                .version(1L)
                .updatedBy(updaterId)
                .build();
    }
}
