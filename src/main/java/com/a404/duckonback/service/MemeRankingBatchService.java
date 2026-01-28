package com.a404.duckonback.service;

public interface MemeRankingBatchService {
    /**
     * 직전 1시간 구간 기준으로 밈 사용 로그를 집계해
     * meme_hourly_top10 테이블에 Top10을 저장한다.
     */
    void aggregateHourlyTopMemes();
}
