-- Master user table
CREATE TABLE IF NOT EXISTS users (
                                     id INTEGER PRIMARY KEY AUTOINCREMENT,
                                     username TEXT NOT NULL UNIQUE,
                                     salt BLOB NOT NULL,
                                     hashed_password BLOB NOT NULL,
                                     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                     last_login TIMESTAMP
);

-- Credentials table
CREATE TABLE IF NOT EXISTS credentials (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           title TEXT NOT NULL,
                                           username TEXT,
                                           email TEXT,
                                           url TEXT,
                                           encrypted_password BLOB NOT NULL,
                                           encryption_iv BLOB NOT NULL,
                                           notes TEXT,
                                           tags TEXT,
                                           is_favorite INTEGER DEFAULT 0,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                           last_modified TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- File vaults table
CREATE TABLE IF NOT EXISTS file_vaults (
                                           id INTEGER PRIMARY KEY AUTOINCREMENT,
                                           vault_name TEXT NOT NULL,
                                           vault_type TEXT NOT NULL,
                                           vault_password_hash BLOB,
                                           vault_salt BLOB,
                                           icon_emoji TEXT,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Encrypted files table
CREATE TABLE IF NOT EXISTS encrypted_files (
                                               id INTEGER PRIMARY KEY AUTOINCREMENT,
                                               vault_id INTEGER NOT NULL,
                                               original_file_name TEXT NOT NULL,
                                               encrypted_file_name TEXT NOT NULL,
                                               original_size INTEGER NOT NULL,
                                               encrypted_size INTEGER NOT NULL,
                                               mime_type TEXT,
                                               encryption_iv BLOB,
                                               checksum TEXT NOT NULL,
                                               uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                               last_accessed TIMESTAMP,
                                               FOREIGN KEY (vault_id) REFERENCES file_vaults(id) ON DELETE CASCADE
    );

-- Backups table
CREATE TABLE IF NOT EXISTS backups (
                                       id INTEGER PRIMARY KEY AUTOINCREMENT,
                                       backup_file_name TEXT NOT NULL,
                                       backup_path TEXT NOT NULL,
                                       file_size INTEGER NOT NULL,
                                       checksum TEXT NOT NULL,
                                       backup_type TEXT NOT NULL,
                                       status TEXT NOT NULL,
                                       description TEXT,
                                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit log table
CREATE TABLE IF NOT EXISTS audit_log (
                                         id INTEGER PRIMARY KEY AUTOINCREMENT,
                                         user_id INTEGER,
                                         action TEXT NOT NULL,
                                         entity_type TEXT NOT NULL,
                                         entity_id INTEGER,
                                         details TEXT,
                                         ip_address TEXT,
                                         timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         FOREIGN KEY (user_id) REFERENCES users(id)
    );

-- Gamification tables
CREATE TABLE IF NOT EXISTS missions (
                                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                                        mission_name TEXT NOT NULL,
                                        description TEXT NOT NULL,
                                        points INTEGER NOT NULL,
                                        badge_emoji TEXT,
                                        difficulty_level TEXT NOT NULL,
                                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_missions (
                                             id INTEGER PRIMARY KEY AUTOINCREMENT,
                                             user_id INTEGER NOT NULL,
                                             mission_id INTEGER NOT NULL,
                                             completed INTEGER DEFAULT 0,
                                             completed_at TIMESTAMP,
                                             FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (mission_id) REFERENCES missions(id)
    );

-- Indexes for performance
CREATE INDEX IF NOT EXISTS idx_credentials_title ON credentials(title);
CREATE INDEX IF NOT EXISTS idx_credentials_favorite ON credentials(is_favorite);
CREATE INDEX IF NOT EXISTS idx_credentials_modified ON credentials(last_modified);
CREATE INDEX IF NOT EXISTS idx_encrypted_files_vault ON encrypted_files(vault_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_user ON audit_log(user_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_timestamp ON audit_log(timestamp);