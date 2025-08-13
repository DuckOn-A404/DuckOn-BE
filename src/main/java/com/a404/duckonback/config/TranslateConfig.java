package com.a404.duckonback.config;

import com.a404.duckonback.config.ServiceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class TranslateConfig {

    @Bean
    public RestClient translateRestClient(ServiceProperties props) {
        // JDK 기본 구현 – 추가 의존성 없음
        var factory = new SimpleClientHttpRequestFactory();
        int timeout = (int) props.getTranslateTimeoutMs();
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);

        return RestClient.builder()
                .baseUrl(props.getTranslateBaseUrl())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(factory)
                .build();
    }
}
