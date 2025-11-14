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
    private OpenSearchDebugInfo debugInfo; // 삭제제

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MemeInfoDTO {
        private Long memeId;
        private String imageUrl;
        private List<String> tags;

        // 테스트 후 삭제할 것
        private Boolean openSearchIndexed;
        private String openSearchError;
    }

    // 테스트 후 삭제
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OpenSearchDebugInfo {
        private Boolean opensearchConnected;
        private String indexName;
        private Long totalDocuments;
        private String connectionError;
    }
}
