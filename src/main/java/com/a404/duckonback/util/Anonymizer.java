package com.a404.duckonback.util;

import java.util.UUID;

public final class Anonymizer {
    private Anonymizer() {}

    private static String tag() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    public static String email(long id) {
        return "deleted+" + id + "." + tag() + "@example.invalid";
    }
    public static String userId(long id) {
        return "deleted_" + id + "_" + tag();
    }
    public static String nickname() {
        return "탈퇴회원_" + tag().substring(0, 6);
    }
}
