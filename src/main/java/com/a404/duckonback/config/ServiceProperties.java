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

}
