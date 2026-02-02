package com.a404.duckonback.domain.artist.request.dto;

import com.a404.duckonback.common.enums.RequestStatus;
import com.a404.duckonback.domain.artist.request.entity.ArtistChangeTargetType;
import com.a404.duckonback.domain.artist.request.entity.ArtistProfileChangeRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtistChangeRequestInfoDTO {
    private Long id;
    private ArtistChangeTargetType targetType;
    private Long targetId;
    private String artistNameEn;
    private String artistNameKr;

    @Size(max = 2000)
    @NotBlank
    private String content;

    @Size(max = 2000)
    private String attachment;

    private RequestStatus status;

    public static ArtistChangeRequestInfoDTO fromEntity(ArtistProfileChangeRequest e) {
        return ArtistChangeRequestInfoDTO.builder()
                .id(e.getId())
                .targetType(e.getTargetType())
                .targetId(e.getTargetId())
                .artistNameEn(e.getTargetNameEn())
                .artistNameKr(e.getTargetNameKr())
                .status(e.getStatus())
                .content(e.getContent())
                .attachment(e.getAttachment())
                .build();
    }

}
