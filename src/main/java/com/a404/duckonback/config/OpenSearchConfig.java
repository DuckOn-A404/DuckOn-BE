package com.a404.duckonback.config;

import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class OpenSearchConfig {

    @Value("${opensearch.endpoint}")
    private String endpoint;

    @Value("${opensearch.region}")
    private String region;
    
    @Value("${S3_ACCESS_KEY}")
    private String accessKey;
    
    @Value("${S3_SECRET_KEY}")
    private String secretKey;

    @Bean
    public OpenSearchClient openSearchClient() {
        try {
            log.info("🔧 [OpenSearch] OpenSearchClient 초기화 시작");
            log.info("🔧 [OpenSearch] Endpoint: {}", endpoint);
            log.info("🔧 [OpenSearch] Region: {}", region);
            log.info("🔧 [OpenSearch] AccessKey: {}***", accessKey != null && accessKey.length() > 4 ? accessKey.substring(0, 4) : "null");

            // SdkHttpClient 생성
            SdkHttpClient httpClient = ApacheHttpClient.builder().build();

            // AWS 자격증명 생성 (env.properties에서 읽어온 값 사용)
            AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
            StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
            log.info("🔧 [OpenSearch] AWS 자격증명 생성 완료");

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
            log.info("🔧 [OpenSearch] Transport 생성 완료");

            OpenSearchClient client = new OpenSearchClient(transport);
            log.info("✅ [OpenSearch] OpenSearchClient 초기화 성공");
            return client;
        } catch (Exception e) {
            log.error("❌ [OpenSearch] OpenSearchClient 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("OpenSearch 클라이언트 초기화 실패", e);
        }
    }
}