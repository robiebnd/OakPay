package com.oakpay.trading.p2p;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlatformFeeSettingRepository extends JpaRepository<PlatformFeeSetting, String> {
    Optional<PlatformFeeSetting> findBySettingKey(String settingKey);
}
