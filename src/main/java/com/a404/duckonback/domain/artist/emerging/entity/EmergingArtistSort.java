package com.a404.duckonback.domain.artist.emerging.entity;

public enum EmergingArtistSort {
    CREATED, NAME, DEBUT, FOLLOWERS;

    public static EmergingArtistSort parse(String sort) {
        if (sort == null) return CREATED;

        return switch (sort.toLowerCase()) {
            case "created" -> CREATED;
            case "name" -> NAME;
            case "debut" -> DEBUT;
            case "followers" -> FOLLOWERS;
            default -> FOLLOWERS;
        };
    }
}
