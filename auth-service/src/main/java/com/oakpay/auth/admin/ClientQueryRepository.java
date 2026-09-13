package com.oakpay.auth.admin;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClientQueryRepository extends JpaRepository<ClientQuery, UUID> {

    List<ClientQuery> findAllByOrderByCreatedAtDesc();

    List<ClientQuery> findByStatusOrderByCreatedAtDesc(String status);
}