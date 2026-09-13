package com.oakpay.auth.admin;

public record AdminDashboard(
        long pendingKyc,
        long openQueries,
        long activeDisputes,
        long pendingResolutions
) {
}