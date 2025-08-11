package com.a404.duckonback.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TranslateRequest(
        String text,
        String src,
        String tgt,
        @JsonProperty("use_glossary") boolean useGlossary
) {}