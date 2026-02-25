package com.a404.duckonback.domain.artist.common;

import com.a404.duckonback.domain.artist.request.entity.ArtistChangeTargetType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistSummaryDTO {
    private ArtistChangeTargetType targetType;
    private Long id;
    private String nameEn;
    private String nameKr;
}
