CREATE SEQUENCE IF NOT EXISTS oakpay_tatum_address_index_seq
    START WITH 0
    INCREMENT BY 1
    MINVALUE 0;

GRANT USAGE, SELECT ON SEQUENCE oakpay_tatum_address_index_seq TO oakpay;
