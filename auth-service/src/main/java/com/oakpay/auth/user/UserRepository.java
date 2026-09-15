package com.oakpay.auth.user;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    List<User> findAllByOrderByCreatedAtDesc();

    List<User> findByStatusOrderByCreatedAtDesc(String status);

    List<User> findByRoleOrderByCreatedAtDesc(String role);

    long countByStatus(String status);

    long countByRole(String role);
}
