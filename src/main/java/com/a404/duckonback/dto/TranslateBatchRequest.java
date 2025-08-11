package com.a404.duckonback.dto;

import java.util.List;

public record TranslateBatchRequest(
        List<TranslateRequest> items
) {}