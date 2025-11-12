package com.a404.duckonback.config;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

@Configuration
public class OpenSearchConfig {

    @Value("${opensearch.endpoint}")
    private String endpoint;

    @Value("${opensearch.region}")
    private String region;

    @Bean
    public OpenSearchClient openSearchClient() {
        // SdkHttpClient 생성
        SdkHttpClient httpClient = ApacheHttpClient.builder().build();

        // AWS 자격증명 프로바이더
        DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.builder().build();

        // AwsSdk2Transport 생성 - Region을 생성자에서 직접 전달
        AwsSdk2Transport transport = new AwsSdk2Transport(
                httpClient,
                endpoint,           // OpenSearch 엔드포인트 (https:// 제외)
                "es",              // 서비스 이름
                Region.of(region), // Region 객체
                AwsSdk2TransportOptions.builder()
                        .setCredentials(credentialsProvider)
                        .build()
        );

        return new OpenSearchClient(transport);
    }
}