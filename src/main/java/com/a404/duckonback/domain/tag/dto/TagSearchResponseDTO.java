package com.a404.duckonback.domain.tag.dto;

import com.a404.duckonback.domain.tag.entity.Tag;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagSearchResponseDTO {
    private Long tagId;
    private String tagName;

    public static TagSearchResponseDTO fromEntity(Tag tag) {
        return TagSearchResponseDTO.builder()
                .tagId(tag.getId())
                .tagName(tag.getTagName())
                .build();
    }
}
