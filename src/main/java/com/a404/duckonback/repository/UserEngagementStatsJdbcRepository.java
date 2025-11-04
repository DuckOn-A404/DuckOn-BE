package com.a404.duckonback.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserEngagementStatsJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    // 단일 유저 스냅샷 조회
    public Optional<SnapshotRow> findByUserPk(Long userPk) {
        List<SnapshotRow> rows = jdbcTemplate.query("""
            SELECT user_id, room_create_count, grade_composite
            FROM user_engagement_stats
            WHERE user_id = ?
        """, (rs, i) -> mapSnapshot(rs), userPk);
        return rows.stream().findFirst();
    }

    // 리더보드 (합성 퍼센타일 내림차순) + 유저 조인
    public List<LeaderboardRow> findLeaderboard(int limit, int offset) {
        return jdbcTemplate.query("""
            SELECT
              u.user_id          AS public_user_id,
              u.nickname         AS nickname,
              u.img_url          AS img_url,
              s.room_create_count AS room_create_count,
              s.grade_composite   AS grade_composite
            FROM user_engagement_stats s
            JOIN `user` u ON u.id = s.user_id AND u.deleted = FALSE
            ORDER BY s.p_composite DESC, s.user_id ASC
            LIMIT ? OFFSET ?
        """, (rs, i) -> mapLeaderboard(rs), limit, offset);
    }

    private SnapshotRow mapSnapshot(ResultSet rs) throws SQLException {
        SnapshotRow row = new SnapshotRow();
        row.userIdPk = rs.getLong("user_id");
        row.roomCreateCount = rs.getLong("room_create_count");
        row.gradeComposite = rs.getString("grade_composite");
        return row;
    }

    private LeaderboardRow mapLeaderboard(ResultSet rs) throws SQLException {
        LeaderboardRow row = new LeaderboardRow();
        row.publicUserId = rs.getString("public_user_id");
        row.nickname = rs.getString("nickname");
        row.imgUrl = rs.getString("img_url");
        row.roomCreateCount = rs.getLong("room_create_count");
        row.gradeComposite = rs.getString("grade_composite");
        return row;
    }

    // ---- Projections ----
    public static class SnapshotRow {
        public Long userIdPk;
        public Long roomCreateCount;
        public String gradeComposite;
    }

    public static class LeaderboardRow {
        public String publicUserId;
        public String nickname;
        public String imgUrl;
        public Long roomCreateCount;
        public String gradeComposite;
    }
}
