package com.a404.duckonback.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemeUpdateRequestDTO {
    @NotNull(message = "태그 목록은 필수입니다.")
    @Size(min=1, max=25, message = "태그는 최소 1개, 최대 25개까지 가능합니다.")
    private List<String> tags;
}
