package com.a404.duckonback.common.util;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

import java.util.concurrent.TimeUnit;

public class HttpCacheUtil {

    private HttpCacheUtil(){}

    public static String weakEtag(String prefix, long version){
        return "W/\"" + prefix + "-" + version + "\"";
    }

    public static boolean isNotModified(String ifNoneMatch, String currentEtag) {
        if (ifNoneMatch == null || currentEtag == null || currentEtag.isBlank())  return false;

        String[] candidates = ifNoneMatch.split(",");
        for(String c : candidates){
            if(c.trim().equals(currentEtag.trim())) return true;
        }

        return false;
    }

    public static ResponseEntity<Void> notModified(String etag, long maxAgeSeconds){
        return ResponseEntity.status(304)
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(maxAgeSeconds, TimeUnit.SECONDS).cachePublic())
                .build();
    }

    public static <T> ResponseEntity<T> ok(T body, String etag, long maxAgeSeconds){
        return ResponseEntity.ok()
                .eTag(etag)
                .cacheControl(CacheControl.maxAge(maxAgeSeconds, TimeUnit.SECONDS).cachePublic())
                .body(body);
    }
}
