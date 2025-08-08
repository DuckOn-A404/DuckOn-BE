package com.a404.duckonback.service;

import com.a404.duckonback.config.BlacklistProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TokenKeyEncoder {

    private final BlacklistProperties props;

    public TokenKeyEncoder(BlacklistProperties props) {
        this.props = props;
    }

    public String encode(String token) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    props.getHmacSecret().getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"
            ));
            byte[] digest = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
            // URL-safe + padding 제거 → Redis 키로 깔끔
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to HMAC token", e);
        }
    }
}
