// MemeCreateResponseDTO.java
package com.a404.duckonback.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemeCreateResponseDTO {

    private List<MemeInfoDTO> memes;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemeInfoDTO {
        private Long memeId;
        private String imageUrl;
        private List<String> tags;
    }
}
