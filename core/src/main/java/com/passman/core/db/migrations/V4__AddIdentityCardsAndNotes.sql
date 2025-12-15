-- Secure Notes table
CREATE TABLE IF NOT EXISTS secure_notes (
                                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                                            title TEXT NOT NULL,
                                            encrypted_content BLOB NOT NULL,
                                            encryption_iv BLOB NOT NULL,
                                            category TEXT NOT NULL,
                                            tags TEXT,
                                            is_favorite INTEGER DEFAULT 0,
                                            has_attachments INTEGER DEFAULT 0,
                                            color_code TEXT,
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Note attachments table
CREATE TABLE IF NOT EXISTS note_attachments (
                                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                note_id INTEGER NOT NULL,
                                                original_file_name TEXT NOT NULL,
                                                encrypted_file_name TEXT NOT NULL,
                                                file_size INTEGER NOT NULL,
                                                mime_type TEXT,
                                                checksum TEXT NOT NULL,
                                                uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                                FOREIGN KEY (note_id) REFERENCES secure_notes(id) ON DELETE CASCADE
    );

-- Identity Cards table
CREATE TABLE IF NOT EXISTS identity_cards (
                                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                                              card_type TEXT NOT NULL,
                                              card_name TEXT NOT NULL,
                                              encrypted_data BLOB NOT NULL,
                                              encryption_iv BLOB NOT NULL,
                                              card_number_last4 TEXT,
                                              issuing_country TEXT,
                                              issuing_authority TEXT,
                                              issue_date DATE,
                                              expiry_date DATE,
                                              has_photo INTEGER DEFAULT 0,
                                              encrypted_photo BLOB,
                                              photo_encryption_iv BLOB,
                                              is_expired INTEGER DEFAULT 0,
                                              tags TEXT,
                                              color_code TEXT,
                                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                              last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Expiry alerts table
CREATE TABLE IF NOT EXISTS expiry_alerts (
                                             id INTEGER PRIMARY KEY AUTOINCREMENT,
                                             identity_card_id INTEGER NOT NULL,
                                             alert_days_before INTEGER NOT NULL,
                                             alert_sent INTEGER DEFAULT 0,
                                             alert_date TIMESTAMP,
                                             FOREIGN KEY (identity_card_id) REFERENCES identity_cards(id) ON DELETE CASCADE
    );

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_secure_notes_category ON secure_notes(category);
CREATE INDEX IF NOT EXISTS idx_secure_notes_favorite ON secure_notes(is_favorite);
CREATE INDEX IF NOT EXISTS idx_secure_notes_modified ON secure_notes(last_modified);
CREATE INDEX IF NOT EXISTS idx_identity_cards_type ON identity_cards(card_type);
CREATE INDEX IF NOT EXISTS idx_identity_cards_expiry ON identity_cards(expiry_date);
CREATE INDEX IF NOT EXISTS idx_note_attachments_note ON note_attachments(note_id);