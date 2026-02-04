package com.a404.duckonback.domain.artist.request.dto;

import com.a404.duckonback.common.enums.AdminReviewAction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ArtistChangeRequestAdminReviewRequestDTO {
    @NotNull
    private AdminReviewAction action;

    @NotBlank
    private String reviewComment;
}
