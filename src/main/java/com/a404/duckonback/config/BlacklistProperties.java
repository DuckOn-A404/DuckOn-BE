package com.a404.duckonback.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Validated
@ConfigurationProperties(prefix = "security.blacklist")
public class BlacklistProperties {

    @NotBlank
    private String hmacSecret;            // 반드시 환경변수로 주입
    private String keyPrefix = "blacklist:"; // 기본 프리픽스

}
