package com.a404.duckonback.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDocument {
    // OpenSearch 인덱스에 저장된 필드명과 일치시키기
    private String s3_url;
    private String object_key;
    private List<String> tags;
    private LocalDateTime created_at;
}
