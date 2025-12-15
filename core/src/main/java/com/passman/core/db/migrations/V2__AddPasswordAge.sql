-- Add password age tracking
ALTER TABLE credentials ADD COLUMN password_changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- Add password strength score
ALTER TABLE credentials ADD COLUMN password_strength_score INTEGER DEFAULT 0;

-- Add breach detection flag
ALTER TABLE credentials ADD COLUMN is_breached INTEGER DEFAULT 0;

-- Update existing records
UPDATE credentials SET password_changed_at = created_at WHERE password_changed_at IS NULL;