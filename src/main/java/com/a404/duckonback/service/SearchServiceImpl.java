package com.a404.duckonback.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.a404.duckonback.dto.ImageDocument;
import com.a404.duckonback.dto.SearchResponseDto;

import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.query_dsl.Query;
import org.opensearch.client.opensearch.core.IndexRequest;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final OpenSearchClient openSearchClient;
    private final String INDEX_NAME = "images-index";
    
    @Override
    public SearchResponseDto searchByTag(String queryTerm, String mode) throws IOException {
        Query query;
        String normalizedTerm = queryTerm.toLowerCase();

        if ("prefix".equalsIgnoreCase(mode)) {
            query = new Query.Builder()
                .prefix(p -> p
                    .field("tags")
                    .value(normalizedTerm)
                )
                .build();
        } else{
            query = new Query.Builder()
                .term(t -> t
                    .field("tags")
                    .value(normalizedTerm)
                )
                    .build();
        }

        SearchRequest searchRequest = new SearchRequest.Builder()
            .index(INDEX_NAME)
            .query(query)
            .build();
        
        SearchResponse<ImageDocument> response = openSearchClient.search(searchRequest, , ImageDocument.class);

        List<ImageDocument> results = response.hits().hits().stream()
            .map(Hit::source)
            .collect(Collectors.toList());
        
        return SearchResponseDto.builder()
            .queryMode(mode)
            .queryTerm(queryTerm)
            .totalHits(response.hits().total().value())
            .images(results)
            .build();
    }


    @Override
    public void indexImage(ImageDocument imageDocument) throws IOException {
        IndexRequest<ImageDocument> indexeRequest = new IndexRequest.Builder<ImageDocument>()
            .index(INDEX_NAME)
            .id(imageDocument.getObject_key())
            .document(imageDocument)
            .build();

        openSearchClient.index(indexeRequest);
    }

    

}
