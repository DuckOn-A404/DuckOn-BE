package com.a404.duckonback.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RankLevel {
    VIP("VIP", "gold"),
    GOLD("GOLD", "amber"),
    PURPLE("PURPLE", "purple"),
    YELLOW("YELLOW", "yellow"),
    NORMAL("NORMAL", "gray");

    private final String label;
    private final String color;

    public static RankLevel fromString(String s) {
        if (s == null) return NORMAL;
        try { return RankLevel.valueOf(s.toUpperCase()); }
        catch (IllegalArgumentException e) { return NORMAL; }
    }
}
