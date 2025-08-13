package com.a404.duckonback.util;

import com.github.pemistahl.lingua.api.IsoCode639_1;
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;

public class LanguageDetector {

    private static final com.github.pemistahl.lingua.api.LanguageDetector detector =
            LanguageDetectorBuilder.fromAllLanguages().build();

    public static String detect(String text) {
        if (text == null || text.isBlank()) return "en";
        try {
            Language lang = detector.detectLanguageOf(text);
            IsoCode639_1 iso = lang.getIsoCode639_1();
            return iso == null ? "en" : iso.toString().toLowerCase(); // "en","ko","ja" 등
        } catch (Exception e) {
            return "en";
        }
    }
}
