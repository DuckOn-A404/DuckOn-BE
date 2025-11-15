package com.a404.duckonback.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;


/**
 * 매일 00:00 (Asia/Seoul) 에 유저 참여도 통계(user_engagement_stats) 재집계 배치
 * - room / meme MySQL
 * - chat MongoDB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EngagementBatchServiceImpl implements EngagementBatchService {

    private final JdbcTemplate jdbcTemplate;
    private final MongoTemplate mongoTemplate;

    /**
     * 매일 자정(한국시간)에 실행
     */
    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Transactional
    @Override
    public void rebuildEngagementSnapshot() {
        log.info("[Batch] Rebuilding user engagement stats snapshot...");

        // 1) 임시 테이블 초기화
        jdbcTemplate.update("DROP TEMPORARY TABLE IF EXISTS tmp_counts");
        jdbcTemplate.update("""
            CREATE TEMPORARY TABLE tmp_counts
            SELECT 
                u.id AS user_id,
                0    AS room_create_count,
                0    AS meme_create_count,
                0    AS artist_chat_count
            FROM `user` u
            WHERE u.deleted = FALSE
              -- ✅ 관리자 제외 (실제 컬럼/값에 맞게 수정)
              AND u.role NOT IN ('ADMIN', 'ROLE_ADMIN')
        """);

        // 2) room 생성 수
        jdbcTemplate.update("""
            UPDATE tmp_counts tc
            JOIN (
                SELECT r.creator_id AS user_id, COUNT(*) AS cnt
                FROM room r
                GROUP BY r.creator_id
            ) rc ON rc.user_id = tc.user_id
            SET tc.room_create_count = rc.cnt
        """);

        // 3) meme 생성 수
        jdbcTemplate.update("""
            UPDATE tmp_counts tc
            JOIN (
                SELECT m.creator_id AS user_id, COUNT(*) AS cnt
                FROM meme m
                GROUP BY m.creator_id
            ) mc ON mc.user_id = tc.user_id
            SET tc.meme_create_count = mc.cnt
        """);

        // 4) chat 메시지 수 (MongoDB → tmp_counts 반영)
        final String collection = "artist_chats";
        try {
            long docs = mongoTemplate.getDb().getCollection(collection).countDocuments();
            log.info("[Batch] Mongo collection '{}' docs = {}", collection, docs);
        } catch (Exception e) {
            log.warn("[Batch] Mongo collection '{}' check failed: {}", collection, e.getMessage());
        }

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.group("senderId").count().as("cnt")
        );
        AggregationResults<Document> results =
                mongoTemplate.aggregate(agg, collection, Document.class);

        List<Map.Entry<Long, Integer>> chatCounts = results.getMappedResults().stream()
                .filter(d -> d.get("_id") instanceof Number && d.get("cnt") instanceof Number)
                .map(d -> Map.entry(
                        ((Number) d.get("_id")).longValue(),
                        ((Number) d.get("cnt")).intValue()
                ))
                .toList();

        jdbcTemplate.batchUpdate(
                "UPDATE tmp_counts SET artist_chat_count=? WHERE user_id=?",
                chatCounts, 1000,
                (ps, e) -> {
                    ps.setInt(1, e.getValue());
                    ps.setLong(2, e.getKey());
                }
        );

        // 5) 스냅샷 반영 (관리자 제외된 사용자만 대상)
        jdbcTemplate.update("""
            REPLACE INTO user_engagement_stats (
              user_id, room_create_count, artist_chat_count, meme_create_count, updated_at
            )
            SELECT user_id, room_create_count, artist_chat_count, meme_create_count, NOW(6)
            FROM tmp_counts
        """);

        // 6) 퍼센타일 계산용 임시 테이블
        jdbcTemplate.update("DROP TEMPORARY TABLE IF EXISTS tmp_percentiles");
        jdbcTemplate.update("""
            CREATE TEMPORARY TABLE tmp_percentiles AS
            SELECT
              s.user_id,
              PERCENT_RANK() OVER (ORDER BY s.room_create_count  DESC) AS pr,
              PERCENT_RANK() OVER (ORDER BY s.artist_chat_count DESC) AS pc,
              PERCENT_RANK() OVER (ORDER BY s.meme_create_count  DESC) AS pm
            FROM user_engagement_stats s
            JOIN `user` u ON u.id = s.user_id
            WHERE u.deleted = FALSE
              AND u.role NOT IN ('ADMIN', 'ROLE_ADMIN')
        """);

        // 6-1) min/max 임시 테이블
        jdbcTemplate.update("DROP TEMPORARY TABLE IF EXISTS tmp_stats");
        jdbcTemplate.update("""
            CREATE TEMPORARY TABLE tmp_stats AS
            SELECT
              MAX(room_create_count)  AS max_room,
              MIN(room_create_count)  AS min_room,
              MAX(artist_chat_count)  AS max_chat,
              MIN(artist_chat_count)  AS min_chat,
              MAX(meme_create_count)  AS max_meme,
              MIN(meme_create_count)  AS min_meme
            FROM user_engagement_stats s
            JOIN `user` u ON u.id = s.user_id
            WHERE u.deleted = FALSE
              AND u.role NOT IN ('ADMIN', 'ROLE_ADMIN')
        """);

        // 7) 최종 p/grade 갱신
        jdbcTemplate.update("""
            UPDATE user_engagement_stats s
            JOIN tmp_percentiles p ON p.user_id = s.user_id
            CROSS JOIN tmp_stats t
            SET
              -- 개별 퍼센타일 점수 (0~100, 높을수록 상위)
              s.p_room = ROUND(
                CASE WHEN t.max_room = t.min_room THEN 0 ELSE (1 - p.pr) * 100 END
              , 2),
              s.p_chat = ROUND(
                CASE WHEN t.max_chat = t.min_chat THEN 0 ELSE (1 - p.pc) * 100 END
              , 2),
              s.p_meme = ROUND(
                CASE WHEN t.max_meme = t.min_meme THEN 0 ELSE (1 - p.pm) * 100 END
              , 2),

              -- 통합 점수 (가중치: room 0.5, chat 0.3, meme 0.2)
              s.p_composite = ROUND(
                (
                  (CASE WHEN t.max_room <> t.min_room THEN 0.5 * (1 - p.pr) ELSE 0 END) +
                  (CASE WHEN t.max_chat <> t.min_chat THEN 0.3 * (1 - p.pc) ELSE 0 END) +
                  (CASE WHEN t.max_meme <> t.min_meme THEN 0.2 * (1 - p.pm) ELSE 0 END)
                )
                / NULLIF(
                  (CASE WHEN t.max_room <> t.min_room THEN 0.5 ELSE 0 END) +
                  (CASE WHEN t.max_chat <> t.min_chat THEN 0.3 ELSE 0 END) +
                  (CASE WHEN t.max_meme <> t.min_meme THEN 0.2 ELSE 0 END)
                , 0) * 100
              , 2),

              -- room 등급
              s.grade_room = CASE
                WHEN (CASE WHEN t.max_room = t.min_room THEN 0 ELSE (1 - p.pr) * 100 END) >= 95 THEN 'VIP'
                WHEN (CASE WHEN t.max_room = t.min_room THEN 0 ELSE (1 - p.pr) * 100 END) >= 80 THEN 'GOLD'
                WHEN (CASE WHEN t.max_room = t.min_room THEN 0 ELSE (1 - p.pr) * 100 END) >= 70 THEN 'PURPLE'
                WHEN (CASE WHEN t.max_room = t.min_room THEN 0 ELSE (1 - p.pr) * 100 END) >= 60 THEN 'YELLOW'
                ELSE 'GREEN'
              END,

              -- chat 등급
              s.grade_chat = CASE
                WHEN (CASE WHEN t.max_chat = t.min_chat THEN 0 ELSE (1 - p.pc) * 100 END) >= 95 THEN 'VIP'
                WHEN (CASE WHEN t.max_chat = t.min_chat THEN 0 ELSE (1 - p.pc) * 100 END) >= 80 THEN 'GOLD'
                WHEN (CASE WHEN t.max_chat = t.min_chat THEN 0 ELSE (1 - p.pc) * 100 END) >= 70 THEN 'PURPLE'
                WHEN (CASE WHEN t.max_chat = t.min_chat THEN 0 ELSE (1 - p.pc) * 100 END) >= 60 THEN 'YELLOW'
                ELSE 'GREEN'
              END,

              -- meme 등급
              s.grade_meme = CASE
                WHEN (CASE WHEN t.max_meme = t.min_meme THEN 0 ELSE (1 - p.pm) * 100 END) >= 95 THEN 'VIP'
                WHEN (CASE WHEN t.max_meme = t.min_meme THEN 0 ELSE (1 - p.pm) * 100 END) >= 80 THEN 'GOLD'
                WHEN (CASE WHEN t.max_meme = t.min_meme THEN 0 ELSE (1 - p.pm) * 100 END) >= 70 THEN 'PURPLE'
                WHEN (CASE WHEN t.max_meme = t.min_meme THEN 0 ELSE (1 - p.pm) * 100 END) >= 60 THEN 'YELLOW'
                ELSE 'GREEN'
              END

        """);

        // 8) p_composite 기반 퍼센타일 등급 산정 (ADMIN 제외)
        jdbcTemplate.update("DROP TEMPORARY TABLE IF EXISTS tmp_composite_pct");
        jdbcTemplate.update("""
            CREATE TEMPORARY TABLE tmp_composite_pct AS
            SELECT
              s.user_id,
              PERCENT_RANK() OVER (ORDER BY s.p_composite DESC) AS pr_comp
            FROM user_engagement_stats s
            JOIN `user` u ON u.id = s.user_id
            WHERE u.deleted = FALSE
              AND u.role NOT IN ('ADMIN', 'ROLE_ADMIN')
        """);

        jdbcTemplate.update("""
            UPDATE user_engagement_stats s
            JOIN tmp_composite_pct c ON c.user_id = s.user_id
            SET s.grade_composite = CASE
              WHEN c.pr_comp <= 0.10 THEN 'VIP'    -- 상위 10%
              WHEN c.pr_comp <= 0.30 THEN 'GOLD'   -- 10% ~ 30%
              WHEN c.pr_comp <= 0.55 THEN 'PURPLE' -- 30% ~ 55%
              WHEN c.pr_comp <= 0.80 THEN 'YELLOW' -- 55% ~ 80%
              ELSE 'GREEN'                         -- 나머지
            END
        """);
        log.info("[Batch] User engagement stats snapshot rebuilt successfully");
    }
}

