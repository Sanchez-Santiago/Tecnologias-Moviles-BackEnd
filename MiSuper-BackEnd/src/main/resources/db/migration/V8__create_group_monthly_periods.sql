CREATE TABLE IF NOT EXISTS group_monthly_periods (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    group_id UUID NOT NULL REFERENCES groups(id),
    name VARCHAR(255),
    start_date TIMESTAMP NOT NULL,
    end_date TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    initial_balance NUMERIC(12, 2) NOT NULL DEFAULT 0,
    final_balance NUMERIC(12, 2),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    closed_by UUID REFERENCES users(id),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    cycle_type VARCHAR(20) NOT NULL DEFAULT 'MONTHLY'
);

ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS group_id UUID;
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS name VARCHAR(255);
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS start_date TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS end_date TIMESTAMP;
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'OPEN';
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS initial_balance NUMERIC(12, 2) NOT NULL DEFAULT 0;
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS final_balance NUMERIC(12, 2);
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS created_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS closed_by UUID;
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP NOT NULL DEFAULT now();
ALTER TABLE group_monthly_periods ADD COLUMN IF NOT EXISTS cycle_type VARCHAR(20) NOT NULL DEFAULT 'MONTHLY';
