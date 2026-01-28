package com.a404.duckonback.domain.translation.dto;

public record FrontTranslateRequest(
        String message,
        String language // tgt
) {}

