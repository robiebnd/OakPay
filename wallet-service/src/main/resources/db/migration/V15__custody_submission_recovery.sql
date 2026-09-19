ALTER TABLE custody_operations
    DROP CONSTRAINT IF EXISTS ck_custody_operation_status;

ALTER TABLE custody_operations
    ADD CONSTRAINT ck_custody_operation_status
    CHECK (status IN ('REQUESTED', 'SUBMISSION_UNKNOWN', 'SUBMITTED', 'PROCESSING', 'COMPLETED', 'FAILED'));

CREATE INDEX IF NOT EXISTS idx_custody_operations_recovery
    ON custody_operations(status, updated_at)
    WHERE operation_type = 'WITHDRAWAL'
      AND status IN ('REQUESTED', 'SUBMISSION_UNKNOWN', 'SUBMITTED', 'PROCESSING');
