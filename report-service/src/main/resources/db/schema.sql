CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE IF NOT EXISTS generated_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_type VARCHAR(40) NOT NULL,
    generated_by_email VARCHAR(180) NOT NULL,
    filters TEXT,
    total_records INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL
);
