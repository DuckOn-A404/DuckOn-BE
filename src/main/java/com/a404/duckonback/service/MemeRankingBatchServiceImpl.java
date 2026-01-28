package com.a404.duckonback.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemeRankingBatchServiceImpl implements MemeRankingBatchService {

    private final JdbcTemplate jdbcTemplate;

    /**
     * [스케줄러용]
     * 매 시 정각(Asia/Seoul)에 실행
     * → 직전 1시간 [H-1:00, H:00) 집계
     */
    @Scheduled(cron = "0 0 * * * *", zone = "Asia/Seoul")
    @Transactional
    public void aggregateHourlyTopMemesScheduled() {
        var now = java.time.ZonedDateTime.now(java.time.ZoneId.of("Asia/Seoul"))
                .withMinute(0).withSecond(0).withNano(0);

        LocalDateTime end = now.toLocalDateTime();          // ex) 14:00
        LocalDateTime start = end.minusHours(1);            // ex) 13:00

        aggregateForRange(start, end);
    }

    /**
     * [수동 실행용]
     * 현재 시점 기준 직전 1시간 집계
     * → [now-1h, now)
     */
    @Override
    @Transactional
    public void aggregateHourlyTopMemes() {
        // 가장 최근 로그 시간 기준으로 직전 1시간 구간 계산
        LocalDateTime last = jdbcTemplate.queryForObject(
                "SELECT MAX(created_at) FROM meme_usage_log",
                LocalDateTime.class
        );
        if (last == null) {
            log.info("[Batch] No logs found. Skip.");
            return;
        }

        LocalDateTime end = last.withMinute(0).withSecond(0).withNano(0);
        LocalDateTime start = end.minusHours(1);

        aggregateForRange(start, end);
    }

    /**
     * 공통 로직: 주어진 시간 구간 [start, end) 에 대한 Top10 집계
     */
    @Transactional
    public void aggregateForRange(LocalDateTime start, LocalDateTime end) {
        log.info("[Batch] Aggregating meme hourly top10. range=[{} ~ {})", start, end);

        // hour_start: 이 구간의 시작 시각으로 저장
        LocalDateTime hourStart = start;

        // 동일 구간 데이터 삭제 (재집계 대응)
        jdbcTemplate.update("DELETE FROM meme_hourly_top10 WHERE hour_start = ?", hourStart);

        String sql = """
            INSERT INTO meme_hourly_top10 (
                hour_start, rank_no, meme_id,
                use_count, download_count, total_count,
                updated_at
            )
            SELECT
                ?                                 AS hour_start,
                t.rn                              AS rank_no,
                t.meme_id                         AS meme_id,
                t.use_cnt                         AS use_count,
                t.download_cnt                    AS download_count,
                t.total_cnt                       AS total_count,
                NOW(6)                            AS updated_at
            FROM (
                SELECT
                    mul.meme_id AS meme_id,
                    SUM(CASE WHEN mul.usage_type = 'USE' THEN 1 ELSE 0 END)       AS use_cnt,
                    SUM(CASE WHEN mul.usage_type = 'DOWNLOAD' THEN 1 ELSE 0 END)  AS download_cnt,
                    COUNT(*)                                                      AS total_cnt,
                    ROW_NUMBER() OVER (
                        ORDER BY COUNT(*) DESC, mul.meme_id ASC
                    ) AS rn
                FROM meme_usage_log mul
                WHERE mul.created_at >= ?
                  AND mul.created_at < ?
                GROUP BY mul.meme_id
            ) t
            WHERE t.rn <= 10
            """;

        int inserted = jdbcTemplate.update(sql, ps -> {
            ps.setObject(1, hourStart);
            ps.setObject(2, start);
            ps.setObject(3, end);
        });

        log.info("[Batch] Meme hourly top10 aggregated. hour_start={}, insertedRows={}",
                hourStart, inserted);
    }
}
