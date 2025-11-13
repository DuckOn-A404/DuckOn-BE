package com.a404.duckonback.config;

public final class SecurityEndpoints {
    private SecurityEndpoints() {}

    public static final String[] SWAGGER = {
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/webjars/**"
    };

    public static final String[] WS = {
            "/ws-chat/**"
    };

    public static final String[] PUBLIC_ANY = {
            "/api/memes/top/**",
            "/api/tags/**",
    };

    public static final String[] PUBLIC_GET = {
            // artists
            "/api/artists",
            "/api/artists/random",
            "/api/artists/*",

            // artists chat
            "/api/chat/artist/**",

            // rooms
            "/api/rooms",
            "/api/rooms/*",
            "/api/rooms/trending/*",
            "/api/rooms/home",

            // users
            "/api/users/recommendations",
            "/api/users/leaderboard",

            // youtube
            "/api/public/youtube/meta/*",

            // memes
            "/api/memes/*/detail",
            "/api/memes/top/**",
            "/api/memes/search-basic",
            "/api/memes/random",
            "/api/memes/search"
    };

    public static final String[] PUBLIC_POST = {
            "/api/rooms/*/enter",
            "/api/rooms/*/exit",
            "/api/memes/usage"
    };

    public static final String[] AUTH_REQUIRED = {
            "/api/me/**",
            "/api/auth/logout"
    };

    public static final String[] ADMIN = {
            "/api/admin/**"
    };
}
