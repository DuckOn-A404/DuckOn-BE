package com.a404.duckonback.dto;

public record FrontTranslateRequest(
        String message,
        String language // tgt
) {}

