package com.a404.duckonback.dto;

import com.a404.duckonback.enums.MemeUsageType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemeUsageLogRequestDTO {
    @NotNull
    private Long memeId;

    @NotNull
    private MemeUsageType usageType; // USE, DOWNLOAD
}
