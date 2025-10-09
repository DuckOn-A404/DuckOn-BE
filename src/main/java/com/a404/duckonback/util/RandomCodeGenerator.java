package com.a404.duckonback.util;

import java.security.SecureRandom;

public final class RandomCodeGenerator {
    private static final SecureRandom RND = new SecureRandom();
    private RandomCodeGenerator() {}
    public static String numeric(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i=0; i<length; i++) sb.append(RND.nextInt(10));
        return sb.toString();
    }
}
