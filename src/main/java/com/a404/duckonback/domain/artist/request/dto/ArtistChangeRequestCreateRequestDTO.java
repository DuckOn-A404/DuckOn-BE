package com.a404.duckonback.domain.artist.request.dto;

import com.a404.duckonback.domain.artist.request.entity.ArtistChangeTargetType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistChangeRequestCreateRequestDTO {
    @NotNull @Valid
    private ArtistChangeTargetType targetType; // ARTIST / EMERGING_ARTIST

    @NotNull
    private Long targetId;

    @NotBlank
    private String content;

    private String attachment;  // 링크/이미지URL 등 1개 (없으면 null)
}
