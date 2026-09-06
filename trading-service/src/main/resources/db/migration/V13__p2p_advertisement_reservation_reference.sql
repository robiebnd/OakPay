ALTER TABLE p2p_advertisements
    ADD COLUMN reservation_reference VARCHAR(100);

CREATE UNIQUE INDEX uk_p2p_ad_reservation_reference
    ON p2p_advertisements(reservation_reference)
    WHERE reservation_reference IS NOT NULL;
