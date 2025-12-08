package com.passman.core.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Manages SQLite database connection and initialization.
 */
public class DatabaseManager {

    private static final String DB_NAME = "passman.db";
    private Connection connection;
    private final String dbPath;

    public DatabaseManager() {
        this(getDefaultDatabasePath());
    }

    public DatabaseManager(String dbPath) {
        this.dbPath = dbPath;
    }

    private static String getDefaultDatabasePath() {
        String userHome = System.getProperty("user.home");
        File passmanDir = new File(userHome, ".passman");
        if (!passmanDir.exists()) {
            passmanDir.mkdirs();
        }
        return new File(passmanDir, DB_NAME).getAbsolutePath();
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            initializeTables();
        }
        return connection;
    }

    private void initializeTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            // Users table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    salt BLOB NOT NULL,
                    hashed_password BLOB NOT NULL,
                    created_at TEXT NOT NULL,
                    last_login TEXT
                )
            """);

            // Credentials table
            stmt.execute("""
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
                    created_at TEXT NOT NULL,
                    last_modified TEXT NOT NULL
                )
            """);

            // File Vaults table
            stmt. execute("""
                CREATE TABLE IF NOT EXISTS file_vaults (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    vault_name TEXT NOT NULL UNIQUE,
                    vault_type TEXT NOT NULL,
                    vault_password_hash BLOB NOT NULL,
                    vault_salt BLOB NOT NULL,
                    icon_emoji TEXT,
                    file_count INTEGER DEFAULT 0,
                    total_size INTEGER DEFAULT 0,
                    created_at TEXT NOT NULL,
                    last_accessed_at TEXT,
                    is_locked INTEGER DEFAULT 1
                )
            """);

            // Secure Files table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS secure_files (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    vault_id INTEGER NOT NULL,
                    original_file_name TEXT NOT NULL,
                    encrypted_file_name TEXT NOT NULL UNIQUE,
                    file_extension TEXT,
                    mime_type TEXT,
                    original_size INTEGER,
                    encrypted_size INTEGER,
                    encrypted_file_path TEXT NOT NULL,
                    encryption_iv BLOB NOT NULL,
                    file_hash TEXT NOT NULL,
                    notes TEXT,
                    uploaded_at TEXT NOT NULL,
                    last_accessed_at TEXT,
                    FOREIGN KEY (vault_id) REFERENCES file_vaults(id) ON DELETE CASCADE
                )
            """);

            // Create indices for better performance
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_credentials_title ON credentials(title)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_secure_files_vault ON secure_files(vault_id)");

            // Initialize default file vaults
            initializeDefaultVaults();
        }
    }

    private void initializeDefaultVaults() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("""
                INSERT OR IGNORE INTO file_vaults (vault_name, vault_type, vault_password_hash, vault_salt, icon_emoji, created_at)
                VALUES 
                ('Images Vault', 'IMAGES', X'00', X'00', '🖼️', datetime('now')),
                ('PDFs Vault', 'PDFS', X'00', X'00', '📄', datetime('now')),
                ('Documents Vault', 'DOCUMENTS', X'00', X'00', '📝', datetime('now')),
                ('Others Vault', 'OTHERS', X'00', X'00', '📦', datetime('now'))
            """);
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getDatabasePath() {
        return dbPath;
    }
}