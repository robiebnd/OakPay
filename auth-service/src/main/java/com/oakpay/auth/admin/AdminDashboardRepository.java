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

    public long countActiveDisputes() {
        return count("""
                SELECT COUNT(*)
                FROM p2p_disputes
                WHERE status IN ('OPEN', 'UNDER_REVIEW', 'ESCALATED')
                """);
    }

    public long countPendingResolutions() {
        return count("""
                SELECT COUNT(*)
                FROM p2p_disputes
                WHERE status IN ('OPEN', 'UNDER_REVIEW', 'ESCALATED')
                """);
    }

    private long count(String sql) {
        Long result = jdbcTemplate.queryForObject(sql, Long.class);
        return result == null ? 0L : result;
    }
}