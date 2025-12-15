package com.passman.core.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Singleton Database Manager with connection pooling and migrations
 */
public class DatabaseManager {
    private static volatile DatabaseManager instance;
    private Connection connection;
    private String databasePath;

    private DatabaseManager() {}

    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    public void initialize() throws SQLException {
        try {
            databasePath = getDatabasePathForOS();

            File dbFile = new File(databasePath);
            dbFile.getParentFile().mkdirs();

            String url = "jdbc:sqlite:" + databasePath;
            connection = DriverManager.getConnection(url);
            connection.setAutoCommit(true);

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.execute("PRAGMA journal_mode = WAL;");
            }

            runMigrations();
            initializeDefaultData();

            System.out.println("✅ Database initialized at: " + databasePath);

        } catch (Exception e) {
            throw new SQLException("Failed to initialize database", e);
        }
    }

    private String getDatabasePathForOS() {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");
        Path dbPath;

        if (os. contains("win")) {
            String appData = System.getenv("APPDATA");
            dbPath = Paths.get(appData != null ? appData : userHome, "PassMan", "passman.db");
        } else if (os.contains("mac")) {
            dbPath = Paths.get(userHome, "Library", "Application Support", "PassMan", "passman.db");
        } else {
            dbPath = Paths. get(userHome, ".local", "share", "PassMan", "passman.db");
        }

        return dbPath.toString();
    }

    private void runMigrations() throws Exception {
        List<String> migrationFiles = getMigrationFiles();

        for (String migrationFile : migrationFiles) {
            System.out.println("🔄 Running migration: " + migrationFile);
            executeMigration(migrationFile);
            System.out.println("✅ Migration completed: " + migrationFile);
        }
    }

    private List<String> getMigrationFiles() {
        List<String> migrations = new ArrayList<>();
        migrations.add("/db/migrations/V1__InitialSchema.sql");
        migrations.add("/db/migrations/V2__AddPasswordAge.sql");
        migrations.add("/db/migrations/V3__AddVaultSeparatePasswords.sql");
        migrations. add("/db/migrations/V4__AddIdentityCardsAndNotes.sql");
        return migrations;
    }

    private void executeMigration(String migrationFile) throws Exception {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(migrationFile)))) {

            StringBuilder sql = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                sql.append(line).append(" ");

                if (line.endsWith(";")) {
                    String sqlStatement = sql.toString();
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute(sqlStatement);
                    } catch (SQLException e) {
                        if (e.getMessage().contains("already exists") ||
                                e.getMessage().contains("duplicate column")) {
                            System.out.println("⚠️ Skipping:  " + e.getMessage());
                        } else {
                            throw e;
                        }
                    }
                    sql.setLength(0);
                }
            }
        }
    }

    private void initializeDefaultData() throws SQLException {
        initializeDefaultVaults();
        initializeDefaultMissions();
    }

    private void initializeDefaultVaults() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM file_vaults");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("ℹ️ Default vaults already initialized");
                return;
            }

            stmt.execute("""
                INSERT INTO file_vaults (vault_name, vault_type, vault_password_hash, vault_salt, icon_emoji, has_separate_password, is_locked, created_at)
                VALUES 
                ('Images Vault', 'IMAGES', NULL, NULL, '🖼️', 0, 0, datetime('now')),
                ('PDFs Vault', 'PDFS', NULL, NULL, '📄', 0, 0, datetime('now')),
                ('Documents Vault', 'DOCUMENTS', NULL, NULL, '📝', 0, 0, datetime('now')),
                ('Others Vault', 'OTHERS', NULL, NULL, '📦', 0, 0, datetime('now'))
            """);

            System.out.println("✅ Default vaults initialized");
        }
    }

    private void initializeDefaultMissions() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM missions");
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("ℹ️ Default missions already initialized");
                return;
            }

            stmt.execute("""
                INSERT INTO missions (id, mission_name, description, points, badge_emoji, difficulty_level, created_at)
                VALUES 
                (1, 'Vault Beginner', 'Create your first password entry', 10, '🔰', 'BEGINNER', datetime('now')),
                (2, 'Password Pro', 'Generate 5 strong passwords', 25, '🔐', 'INTERMEDIATE', datetime('now')),
                (3, 'Security Master', 'Enable all security features', 50, '🛡️', 'ADVANCED', datetime('now')),
                (4, 'Backup Guardian', 'Create your first backup', 20, '💾', 'BEGINNER', datetime('now')),
                (5, 'Zero Reuse Hero', 'Eliminate all password reuse', 100, '🏆', 'EXPERT', datetime('now')),
                (6, 'Note Keeper', 'Create 10 secure notes', 15, '📝', 'BEGINNER', datetime('now')),
                (7, 'Identity Protector', 'Add 5 identity cards', 30, '🆔', 'INTERMEDIATE', datetime('now'))
            """);

            System.out.println("✅ Default missions initialized");
        }
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            initialize();
        }
        return connection;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public void beginTransaction() throws SQLException {
        connection.setAutoCommit(false);
    }

    public void commit() throws SQLException {
        connection. commit();
        connection.setAutoCommit(true);
    }

    public void rollback() throws SQLException {
        connection. rollback();
        connection.setAutoCommit(true);
    }

    public DatabaseStatistics getStatistics() throws SQLException {
        DatabaseStatistics stats = new DatabaseStatistics();

        try (Statement stmt = connection. createStatement()) {
            var rs = stmt.executeQuery("SELECT COUNT(*) FROM credentials");
            if (rs.next()) stats.credentialCount = rs.getInt(1);

            rs = stmt.executeQuery("SELECT COUNT(*) FROM secure_notes");
            if (rs.next()) stats.notesCount = rs.getInt(1);

            rs = stmt.executeQuery("SELECT COUNT(*) FROM identity_cards");
            if (rs.next()) stats.identityCardsCount = rs.getInt(1);

            rs = stmt.executeQuery("SELECT COUNT(*) FROM encrypted_files");
            if (rs.next()) stats.encryptedFilesCount = rs.getInt(1);

            rs = stmt.executeQuery("SELECT COUNT(*) FROM backups");
            if (rs.next()) stats.backupsCount = rs.getInt(1);

            File dbFile = new File(databasePath);
            if (dbFile.exists()) {
                stats. databaseSizeMB = dbFile.length() / (1024.0 * 1024.0);
            }
        }

        return stats;
    }

    public static class DatabaseStatistics {
        public int credentialCount;
        public int notesCount;
        public int identityCardsCount;
        public int encryptedFilesCount;
        public int backupsCount;
        public double databaseSizeMB;

        @Override
        public String toString() {
            return String.format(
                    "Database Statistics:\n" +
                            "  Credentials: %d\n" +
                            "  Secure Notes: %d\n" +
                            "  Identity Cards: %d\n" +
                            "  Encrypted Files: %d\n" +
                            "  Backups: %d\n" +
                            "  Database Size:  %.2f MB",
                    credentialCount, notesCount, identityCardsCount,
                    encryptedFilesCount, backupsCount, databaseSizeMB
            );
        }
    }
}