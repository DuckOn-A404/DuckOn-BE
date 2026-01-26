package com.a404.duckonback.domain.tag.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TrendingTagDTO {
    private Long tagId;
    private String tagName;
    private Long count;
}