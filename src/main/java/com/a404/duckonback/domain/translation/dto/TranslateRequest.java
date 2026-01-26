package com.a404.duckonback.domain.translation.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

public record TranslateRequest(
        String text,
        String src,
        String tgt,
        @JsonProperty("use_glossary") boolean useGlossary
) {}