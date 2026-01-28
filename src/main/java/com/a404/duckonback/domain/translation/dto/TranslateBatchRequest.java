package com.a404.duckonback.domain.translation.dto;

import java.util.List;

public record TranslateBatchRequest(
        List<TranslateRequest> items
) {}