package com.a404.duckonback.domain.home.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeSearchPlaceholderUpdateRequestDTO {
    @NotNull
    @Size(min=1, max = 10)
    private List<
                @NotBlank
                @Size(max = 30)
                String
                > items;
}
