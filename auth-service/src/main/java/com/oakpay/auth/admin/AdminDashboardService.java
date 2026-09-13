package com.oakpay.auth.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminDashboardService {

    private final AdminDashboardRepository repository;

    public AdminDashboardService(AdminDashboardRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public AdminDashboard getDashboard() {
        return new AdminDashboard(
                repository.countPendingKyc(),
                repository.countOpenQueries(),
                repository.countActiveDisputes(),
                repository.countPendingResolutions()
        );
    }
}