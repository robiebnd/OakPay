package com.oakpay.auth.admin;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminDashboardRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminDashboardRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public long countPendingKyc() {
        return count("""
                SELECT COUNT(*)
                FROM kyc_profiles
                WHERE status IN ('PENDING', 'UNDER_REVIEW')
                """);
    }

    public long countOpenQueries() {
        return count("""
                SELECT COUNT(*)
                FROM client_queries
                WHERE status IN ('OPEN', 'ASSIGNED', 'ESCALATED')
                """);
    }

    /**
     * P2P disputes are owned by trading-service.
     * They are not stored in the auth-service database.
     *
     * The Admin dashboard will be connected to the real
     * trading-service dispute data separately.
     */
    public long countActiveDisputes() {
        return 0L;
    }

    /**
     * Resolution Centre currently uses the same real P2P
     * dispute source. Until the trading-service admin
     * endpoint is connected, return zero rather than
     * querying a non-existent auth database table.
     */
    public long countPendingResolutions() {
        return 0L;
    }

    private long count(String sql) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result == null ? 0L : result;
    }
}