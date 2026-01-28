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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class OpenSearchConfig {

    @Value("${opensearch.endpoint}")
    private String endpoint;

    @Value("${opensearch.region}")
    private String region;

    @Bean
    public OpenSearchClient openSearchClient() {
        try {
            log.info("🔧 [OpenSearch] OpenSearchClient 초기화 시작 (IAM Role + SigV4 방식)");
            log.info("🔧 [OpenSearch] Endpoint: {}", endpoint);
            log.info("🔧 [OpenSearch] Region: {}", region);

            // SdkHttpClient 생성
            SdkHttpClient httpClient = ApacheHttpClient.builder().build();

            // DefaultCredentialsProvider: EC2 IAM Role을 자동으로 감지
            // EC2 인스턴스에 부여된 duckon-prod-ec2-ssm Role을 사용
            DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();
            log.info("🔧 [OpenSearch] DefaultCredentialsProvider 생성 완료 (EC2 IAM Role 자동 감지)");

            // AwsSdk2Transport 생성 - SigV4 서명 방식으로 OpenSearch 인증
            AwsSdk2Transport transport = new AwsSdk2Transport(
                    httpClient,
                    endpoint,           // OpenSearch 엔드포인트 (https:// 제외)
                    "es",              // 서비스 이름
                    Region.of(region), // Region 객체
                    AwsSdk2TransportOptions.builder()
                            .setCredentials(credentialsProvider)
                            .build()
            );
            log.info("🔧 [OpenSearch] AwsSdk2Transport 생성 완료 (SigV4 서명 사용)");

            OpenSearchClient client = new OpenSearchClient(transport);
            log.info("✅ [OpenSearch] OpenSearchClient 초기화 성공");
            log.info("💡 [OpenSearch] IAM Role 기반 인증 사용 - OpenSearch Dashboards에서 Role Mapping 확인 필요");
            return client;
        } catch (Exception e) {
            log.error("❌ [OpenSearch] OpenSearchClient 초기화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("OpenSearch 클라이언트 초기화 실패", e);
        }
    }

    // ========== 잘못된 접근 (Basic Auth) - 참고용 주석 ==========
    // 
    // ❌ 이 방식은 사용하지 않습니다!
    // EC2에 IAM Role이 부여된 경우, admin 계정은 GUI 접속용일 뿐입니다.
    // 
    // import org.apache.http.HttpHost;
    // import org.apache.http.auth.AuthScope;
    // import org.apache.http.auth.UsernamePasswordCredentials;
    // import org.apache.http.impl.client.BasicCredentialsProvider;
    // import org.opensearch.client.RestClient;
    // import org.opensearch.client.json.jackson.JacksonJsonpMapper;
    // import org.opensearch.client.transport.rest_client.RestClientTransport;
    // 
    // @Value("${opensearch.username}")
    // private String username;
    // 
    // @Value("${opensearch.password}")
    // private String password;
    // 
    // BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    // credentialsProvider.setCredentials(
    //         AuthScope.ANY,
    //         new UsernamePasswordCredentials(username, password)
    // );
    // RestClient restClient = RestClient.builder(
    //         new HttpHost(endpoint, 443, "https")
    // ).setHttpClientConfigCallback(httpClientBuilder -> 
    //     httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
    // ).build();
    // 
    // ============================================================
}