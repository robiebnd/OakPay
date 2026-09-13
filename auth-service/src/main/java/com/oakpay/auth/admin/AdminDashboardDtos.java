package com.oakpay.auth.admin;

public final class AdminDashboardDtos {

    private AdminDashboardDtos() {
    }

    public record DashboardResponse(
            long pendingKyc,
            long openQueries,
            long activeDisputes,
            long pendingResolutions
    ) {
    }
}