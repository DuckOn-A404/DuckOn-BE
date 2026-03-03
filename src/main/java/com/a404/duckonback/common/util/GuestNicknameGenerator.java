package com.a404.duckonback.common.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.List;

@Component
public class GuestNicknameGenerator {

    private static final SecureRandom RND = new SecureRandom();

    private static final List<String> ADJECTIVES = List.of(
            "귀여운", "멋진", "행복한", "슬픈", "용감한",
            "조용한", "활발한", "똑똑한", "친절한", "재미있는",
            "신나는", "느긋한", "사려 깊은", "낙천적인", "섬세한",
            "날뛰는", "차분한", "열정적인", "유쾌한", "따뜻한", "차가운"
    );

    private static final List<String> ANIMALS = List.of(
            "오리", "토끼", "고양이", "강아지", "곰",
            "여우", "늑대", "사자", "호랑이", "펭귄",
            "코끼리", "기린", "원숭이", "판다", "하마",
            "악어", "거북이", "낙타", "코뿔소", "캥거루",
            "코알라", "카피바라", "수달", "다람쥐", "햄스터", "고슴도치"
    );

    public String generateNickname() {
        String adjective = ADJECTIVES.get(RND.nextInt(ADJECTIVES.size()));
        String animal = ANIMALS.get(RND.nextInt(ANIMALS.size()));

        return adjective + " " + animal;
    }
}
