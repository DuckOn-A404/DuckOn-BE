package com.a404.duckonback.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.a404.duckonback.dto.ImageDocument;
import com.a404.duckonback.dto.SearchResponseDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexRequest;
import org.opensearch.client.opensearch.core.SearchRequest;
import org.opensearch.client.opensearch.core.SearchResponse;
import org.opensearch.client.opensearch.core.search.Hit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final OpenSearchClient openSearchClient;
    private final String INDEX_NAME = "memes-index";
    
    @Override
    public SearchResponseDto searchByTag(String queryTerm, String mode) throws IOException {
        try {
            log.info("🔍 [OpenSearch] 검색 시작 - queryTerm: {}, mode: {}, index: {}", queryTerm, mode, INDEX_NAME);

            Query query;
            String normalizedTerm = queryTerm.toLowerCase();

            if ("prefix".equalsIgnoreCase(mode)) {
                query = new Query.Builder()
                    .prefix(p -> p
                        .field("tags")
                        .value(normalizedTerm)
                    )
                    .build();
                log.info("🔍 [OpenSearch] Prefix 쿼리 생성: field=tags, value={}", normalizedTerm);
            } else{
                query = new Query.Builder()
                    .term(t -> t
                        .field("tags")
                        .value(v -> v.stringValue(normalizedTerm))
                    )
                        .build();
                log.info("🔍 [OpenSearch] Term 쿼리 생성: field=tags, value={}", normalizedTerm);
            }

            SearchRequest searchRequest = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(query)
                .build();

            log.info("🔍 [OpenSearch] OpenSearch 검색 요청 전송 중...");
            SearchResponse<ImageDocument> response = openSearchClient.search(searchRequest, ImageDocument.class);

            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;
            log.info("✅ [OpenSearch] 검색 성공 - totalHits: {}, returned: {}", totalHits, response.hits().hits().size());

            List<ImageDocument> results = response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());

            return SearchResponseDto.builder()
                .queryMode(mode)
                .queryTerm(queryTerm)
                .totalHits(totalHits)
                .images(results)
                .build();
        } catch (Exception e) {
            log.error("❌ [OpenSearch] 검색 실패 - queryTerm: {}, mode: {}, error: {}", queryTerm, mode, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void indexImage(ImageDocument imageDocument) throws IOException {
        try {
            log.info("📝 [OpenSearch] 이미지 인덱싱 시작 - index: {}, object_key: {}", INDEX_NAME, imageDocument.getObject_key());
            log.info("📝 [OpenSearch] Document 내용: s3_url={}, tags={}, created_at={}",
                imageDocument.getS3_url(),
                imageDocument.getTags(),
                imageDocument.getCreated_at());

            IndexRequest<ImageDocument> indexeRequest = new IndexRequest.Builder<ImageDocument>()
                .index(INDEX_NAME)
                .id(imageDocument.getObject_key())
                .document(imageDocument)
                .build();

            log.info("📝 [OpenSearch] OpenSearch에 인덱싱 요청 전송 중...");
            var response = openSearchClient.index(indexeRequest);

            log.info("✅ [OpenSearch] 이미지 인덱싱 성공 - id: {}, result: {}, index: {}",
                response.id(),
                response.result(),
                response.index());
        } catch (Exception e) {
            log.error("❌ [OpenSearch] 이미지 인덱싱 실패 - object_key: {}, error: {}",
                imageDocument.getObject_key(),
                e.getMessage(),
                e);
            throw e;
        }
    }

    @Override
    public Map<String, Object> testConnection() throws IOException {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("🧪 [TEST] OpenSearch 클러스터 정보 조회 중...");
            var info = openSearchClient.info();
            
            result.put("status", "connected");
            result.put("cluster_name", info.clusterName());
            result.put("cluster_uuid", info.clusterUuid());
            result.put("version", info.version().number());
            result.put("tagline", info.tagline());
            
            log.info("✅ [TEST] OpenSearch 연결 성공 - version: {}, cluster: {}", 
                info.version().number(), info.clusterName());
            
        } catch (Exception e) {
            log.error("❌ [TEST] OpenSearch 연결 실패: {}", e.getMessage(), e);
            result.put("status", "error");
            result.put("error_message", e.getMessage());
            result.put("error_type", e.getClass().getSimpleName());
        }
        return result;
    }

    @Override
    public Map<String, Object> getAllDocuments() throws IOException {
        Map<String, Object> result = new HashMap<>();
        try {
            log.info("🧪 [TEST] 전체 문서 조회 중 - index: {}", INDEX_NAME);
            
            SearchRequest searchRequest = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(q -> q.matchAll(m -> m))
                .size(100)  // 최대 100개까지 조회
                .build();
            
            SearchResponse<ImageDocument> response = openSearchClient.search(searchRequest, ImageDocument.class);
            
            long totalHits = response.hits().total() != null ? response.hits().total().value() : 0;
            List<ImageDocument> documents = response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
            
            result.put("status", "success");
            result.put("index", INDEX_NAME);
            result.put("total", totalHits);
            result.put("returned", documents.size());
            result.put("documents", documents);
            
            log.info("✅ [TEST] 전체 문서 조회 성공 - total: {}, returned: {}", totalHits, documents.size());
            
        } catch (Exception e) {
            log.error("❌ [TEST] 전체 문서 조회 실패: {}", e.getMessage(), e);
            result.put("status", "error");
            result.put("error_message", e.getMessage());
            result.put("error_type", e.getClass().getSimpleName());
        }
        return result;
    }
}
