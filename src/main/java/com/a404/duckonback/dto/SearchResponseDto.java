package com.a404.duckonback.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class SearchResponseDto {
    private String queryMode;
    private String queryTerm;
    private long totalHits;
    private List<ImageDocument> images;

}
