package com.oakpay.wallet.custody;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;

public interface TatumAddressIndexRepository extends Repository<CustodyOperation, java.util.UUID> {
    @Query(value = "select nextval('oakpay_tatum_address_index_seq')", nativeQuery = true)
    long nextIndex();
}
