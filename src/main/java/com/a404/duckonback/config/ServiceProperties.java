package com.a404.duckonback.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ServiceProperties {

    @Value("${JWT_SECRET_KEY}")
    private String jwtSecret;

    @Value("${JWT_ACCESS_EXPIRATION}")
    private long accessTokenExpiration;

    @Value("${JWT_REFRESH_EXPIRATION}")
    private long refreshTokenExpiration;

    @Value("${SPRING_DATASOURCE_URL}")
    private String dbUrl;

    @Value("${SPRING_DATASOURCE_USERNAME}")
    private String dbUsername;

    @Value("${SPRING_DATASOURCE_PASSWORD}")
    private String dbPassword;

    @Value("${app.frontend.oauth2-success-url}")
    private String oauth2SuccessUrl;

    @Value("${app.frontend.oauth2-failure-url}")
    private String oauth2FailureUrl;

    @Value("${translate.base-url}")
    private String translateBaseUrl;

    @Value("${translate.timeout-ms}")
    private long translateTimeoutMs;

    @Value("${service.mail.from:noreply@duckon.site}")
    private String mailFrom;

    @Value("${service.mail.brand:DUCKON}")
    private String mailBrand;

    @Value("${service.email.code.ttl-seconds:600}")
    private long emailCodeTtlSeconds;

    @Value("${service.email.code.length:6}")
    private int emailCodeLength;

    @Value("${service.email.rate-limit.send.per-10m:5}")
    private int sendPer10m;

    @Value("${service.email.rate-limit.verify.per-1h:10}")
    private int verifyPer1h;

    @Value("${service.email.code.consume-on-success:true}")
    private boolean consumeOnSuccess;

    // --- OpenAI ---
    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-4.1-mini}")
    private String openAiModel;

}
