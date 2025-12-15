-- Add separate password support for vaults
ALTER TABLE file_vaults ADD COLUMN has_separate_password INTEGER DEFAULT 0;
ALTER TABLE file_vaults ADD COLUMN is_locked INTEGER DEFAULT 0;
ALTER TABLE file_vaults ADD COLUMN last_accessed TIMESTAMP;

-- Update existing vaults to not have separate passwords
UPDATE file_vaults
SET has_separate_password = 0,
    is_locked = 0
WHERE has_separate_password IS NULL;