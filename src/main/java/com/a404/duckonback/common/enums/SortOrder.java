package com.a404.duckonback.common.enums;

public enum SortOrder {
    ASC, DESC;

    public static SortOrder parse(String order) {
        if (order == null) return DESC;
        return "asc".equalsIgnoreCase(order) ? ASC : DESC;
    }
}
