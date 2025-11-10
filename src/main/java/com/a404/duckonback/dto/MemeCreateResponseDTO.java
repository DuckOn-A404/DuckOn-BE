package com.a404.duckonback.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemeCreateResponseDTO {

    private List<MemeInfoDTO> memes;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemeInfoDTO {
        private Long memeId;
        private String imageUrl;   // CDN URL
        private Set<String> tags;  // 최종 저장된 태그들
    }
}
