package com.passman.core.db;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for DatabaseManager
 */
public class DatabaseManagerTest {

    private DatabaseManager dbManager;

    @BeforeEach
    public void setUp() throws Exception {
        dbManager = DatabaseManager.getInstance();
        dbManager.initialize();
    }

    @AfterEach
    public void tearDown() throws Exception {
        if (dbManager != null) {
            dbManager.close();
        }
    }

    @Test
    public void testDatabaseInitialization() throws Exception {
        assertNotNull(dbManager. getDatabasePath(), "Database path should not be null");

        Connection conn = dbManager.getConnection();
        assertNotNull(conn, "Connection should not be null");
        assertFalse(conn.isClosed(), "Connection should be open");

        System. out.println("✅ Database initialized at: " + dbManager.getDatabasePath());
    }

    @Test
    public void testDatabaseFileExists() {
        String dbPath = dbManager.getDatabasePath();
        File dbFile = new File(dbPath);

        assertTrue(dbFile.exists(), "Database file should exist");
        assertTrue(dbFile.length() > 0, "Database file should not be empty");

        System.out.println("✅ Database file exists at: " + dbPath);
    }

    @Test
    public void testAllTablesExist() throws Exception {
        Connection conn = dbManager.getConnection();
        Statement stmt = conn.createStatement();

        // Check all tables exist
        String[] tables = {
                "users", "credentials", "file_vaults", "encrypted_files",
                "backups", "audit_log", "missions", "user_missions",
                "secure_notes", "note_attachments", "identity_cards", "expiry_alerts"
        };

        for (String table : tables) {
            ResultSet rs = stmt.executeQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'"
            );
            assertTrue(rs.next(), "Table " + table + " should exist");
            System.out.println("✅ Table exists: " + table);
        }
    }

    @Test
    public void testDefaultVaultsCreated() throws Exception {
        Connection conn = dbManager.getConnection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM file_vaults");
        assertTrue(rs.next(), "Query should return result");
        int count = rs.getInt(1);
        assertEquals(4, count, "Should have 4 default vaults");

        System.out.println("✅ Default vaults created:  " + count);
    }

    @Test
    public void testDefaultMissionsCreated() throws Exception {
        Connection conn = dbManager. getConnection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM missions");
        assertTrue(rs.next(), "Query should return result");
        int count = rs.getInt(1);
        assertTrue(count >= 7, "Should have at least 7 default missions");

        System. out.println("✅ Default missions created: " + count);
    }

    @Test
    public void testForeignKeysEnabled() throws Exception {
        Connection conn = dbManager.getConnection();
        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("PRAGMA foreign_keys");
        assertTrue(rs.next(), "Query should return result");
        int enabled = rs.getInt(1);
        assertEquals(1, enabled, "Foreign keys should be enabled");

        System.out.println("✅ Foreign keys are enabled");
    }

    @Test
    public void testWALModeEnabled() throws Exception {
        Connection conn = dbManager.getConnection();
        Statement stmt = conn. createStatement();

        ResultSet rs = stmt.executeQuery("PRAGMA journal_mode");
        assertTrue(rs.next(), "Query should return result");
        String mode = rs.getString(1);
        assertEquals("wal", mode. toLowerCase(), "Journal mode should be WAL");

        System.out.println("✅ WAL mode is enabled");
    }

    @Test
    public void testDatabaseStatistics() throws Exception {
        DatabaseManager. DatabaseStatistics stats = dbManager. getStatistics();
        assertNotNull(stats, "Statistics should not be null");

        assertTrue(stats. databaseSizeMB >= 0, "Database size should be non-negative");

        System.out.println("\n" + stats.toString());
    }

    @Test
    public void testTransactionSupport() throws Exception {
        Connection conn = dbManager.getConnection();

        // Test begin transaction
        dbManager.beginTransaction();
        assertFalse(conn.getAutoCommit(), "Auto-commit should be disabled during transaction");

        // Insert test data
        Statement stmt = conn.createStatement();
        stmt.execute("INSERT INTO credentials (title, encrypted_password, encryption_iv) VALUES ('Test', X'1234', X'5678')");

        // Rollback
        dbManager.rollback();
        assertTrue(conn.getAutoCommit(), "Auto-commit should be re-enabled after rollback");

        // Verify rollback worked
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM credentials WHERE title = 'Test'");
        rs.next();
        assertEquals(0, rs.getInt(1), "Rolled back data should not exist");

        System. out.println("✅ Transaction support verified");
    }

    @Test
    public void testMigrationsAreIdempotent() throws Exception {
        // Close and reinitialize database (runs migrations again)
        dbManager.close();
        dbManager.initialize();

        // Should still work without errors
        Connection conn = dbManager.getConnection();
        assertNotNull(conn, "Connection should work after re-initialization");

        System.out.println("✅ Migrations are idempotent");
    }

    @Test
    public void testColumnExistsInCredentials() throws Exception {
        Connection conn = dbManager.getConnection();
        Statement stmt = conn.createStatement();

        // Check V2 migration columns
        ResultSet rs = stmt.executeQuery("PRAGMA table_info(credentials)");
        boolean hasPasswordChangedAt = false;
        boolean hasPasswordStrength = false;
        boolean hasIsBreached = false;

        while (rs.next()) {
            String columnName = rs.getString("name");
            if ("password_changed_at".equals(columnName)) hasPasswordChangedAt = true;
            if ("password_strength_score".equals(columnName)) hasPasswordStrength = true;
            if ("is_breached".equals(columnName)) hasIsBreached = true;
        }

        assertTrue(hasPasswordChangedAt, "credentials should have password_changed_at column");
        assertTrue(hasPasswordStrength, "credentials should have password_strength_score column");
        assertTrue(hasIsBreached, "credentials should have is_breached column");

        System.out.println("✅ V2 migration columns verified");
    }

    @Test
    public void testColumnExistsInFileVaults() throws Exception {
        Connection conn = dbManager.getConnection();
        Statement stmt = conn. createStatement();

        // Check V3 migration columns
        ResultSet rs = stmt.executeQuery("PRAGMA table_info(file_vaults)");
        boolean hasHasSeparatePassword = false;
        boolean hasIsLocked = false;
        boolean hasLastAccessed = false;

        while (rs.next()) {
            String columnName = rs.getString("name");
            if ("has_separate_password".equals(columnName)) hasHasSeparatePassword = true;
            if ("is_locked".equals(columnName)) hasIsLocked = true;
            if ("last_accessed". equals(columnName)) hasLastAccessed = true;
        }

        assertTrue(hasHasSeparatePassword, "file_vaults should have has_separate_password column");
        assertTrue(hasIsLocked, "file_vaults should have is_locked column");
        assertTrue(hasLastAccessed, "file_vaults should have last_accessed column");

        System.out.println("✅ V3 migration columns verified");
    }

    @Test
    public void testV4TablesExist() throws Exception {
        Connection conn = dbManager.getConnection();
        Statement stmt = conn.createStatement();

        // Check V4 tables
        ResultSet rs = stmt. executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='secure_notes'");
        assertTrue(rs.next(), "secure_notes table should exist");

        rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='note_attachments'");
        assertTrue(rs.next(), "note_attachments table should exist");

        rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='identity_cards'");
        assertTrue(rs.next(), "identity_cards table should exist");

        rs = stmt.executeQuery("SELECT name FROM sqlite_master WHERE type='table' AND name='expiry_alerts'");
        assertTrue(rs. next(), "expiry_alerts table should exist");

        System.out.println("✅ V4 migration tables verified");
    }
}