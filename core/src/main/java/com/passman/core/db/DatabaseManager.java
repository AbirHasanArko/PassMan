package com.passman.core.db;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

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
                line = line. trim();

                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }

                sql.append(line).append(" ");

                // Execute when we find a semicolon
                if (line.endsWith(";")) {
                    String sqlStatement = sql.toString().trim();

                    if (! sqlStatement.isEmpty()) {
                        try (Statement stmt = connection. createStatement()) {
                            stmt.execute(sqlStatement);
                            System.out.println("   Executed: " + sqlStatement. substring(0, Math.min(50, sqlStatement.length())) + "...");
                        } catch (SQLException e) {
                            // Only skip if table/column already exists
                            if (e. getMessage().contains("already exists") ||
                                    e.getMessage().contains("duplicate column") ||
                                    e. getMessage().contains("duplicate table")) {
                                System.out.println("⚠️ Skipping:  " + e.getMessage());
                            } else {
                                System.err.println("❌ Failed to execute:  " + sqlStatement);
                                throw e;
                            }
                        }
                    }

                    // Reset for next statement
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

    /**
     * Returns a connection wrapper that delegates all operations to the singleton connection
     * but ignores close() calls, preventing accidental closure of the shared connection.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            initialize();
        }
        return new NonClosingConnectionWrapper(connection);
    }

    /**
     * Get the raw connection directly (for internal use only).
     * This should only be used by methods that need to actually close the connection.
     */
    Connection getRawConnection() throws SQLException {
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
        if (connection != null && !connection.isClosed() && ! connection.getAutoCommit()) {
            try {
                connection.rollback();
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public DatabaseStatistics getStatistics() throws SQLException {
        DatabaseStatistics stats = new DatabaseStatistics();

        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
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

    /**
     * A Connection wrapper that delegates all operations to the underlying connection
     * but ignores close() calls to prevent accidental closure of the shared connection.
     */
    private static class NonClosingConnectionWrapper implements Connection {
        private final Connection delegate;

        public NonClosingConnectionWrapper(Connection delegate) {
            this.delegate = delegate;
        }

        @Override
        public void close() throws SQLException {
            // Intentionally ignore close() to prevent accidental closure
        }

        // Delegate all other methods to the underlying connection
        @Override public Statement createStatement() throws SQLException { return delegate.createStatement(); }
        @Override public PreparedStatement prepareStatement(String sql) throws SQLException { return delegate.prepareStatement(sql); }
        @Override public CallableStatement prepareCall(String sql) throws SQLException { return delegate.prepareCall(sql); }
        @Override public String nativeSQL(String sql) throws SQLException { return delegate.nativeSQL(sql); }
        @Override public void setAutoCommit(boolean autoCommit) throws SQLException { delegate.setAutoCommit(autoCommit); }
        @Override public boolean getAutoCommit() throws SQLException { return delegate.getAutoCommit(); }
        @Override public void commit() throws SQLException { delegate.commit(); }
        @Override public void rollback() throws SQLException { delegate.rollback(); }
        @Override public boolean isClosed() throws SQLException { return delegate.isClosed(); }
        @Override public DatabaseMetaData getMetaData() throws SQLException { return delegate.getMetaData(); }
        @Override public void setReadOnly(boolean readOnly) throws SQLException { delegate.setReadOnly(readOnly); }
        @Override public boolean isReadOnly() throws SQLException { return delegate.isReadOnly(); }
        @Override public void setCatalog(String catalog) throws SQLException { delegate.setCatalog(catalog); }
        @Override public String getCatalog() throws SQLException { return delegate.getCatalog(); }
        @Override public void setTransactionIsolation(int level) throws SQLException { delegate.setTransactionIsolation(level); }
        @Override public int getTransactionIsolation() throws SQLException { return delegate.getTransactionIsolation(); }
        @Override public SQLWarning getWarnings() throws SQLException { return delegate.getWarnings(); }
        @Override public void clearWarnings() throws SQLException { delegate.clearWarnings(); }
        @Override public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException { return delegate.createStatement(resultSetType, resultSetConcurrency); }
        @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency); }
        @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return delegate.prepareCall(sql, resultSetType, resultSetConcurrency); }
        @Override public Map<String, Class<?>> getTypeMap() throws SQLException { return delegate.getTypeMap(); }
        @Override public void setTypeMap(Map<String, Class<?>> map) throws SQLException { delegate.setTypeMap(map); }
        @Override public void setHoldability(int holdability) throws SQLException { delegate.setHoldability(holdability); }
        @Override public int getHoldability() throws SQLException { return delegate.getHoldability(); }
        @Override public Savepoint setSavepoint() throws SQLException { return delegate.setSavepoint(); }
        @Override public Savepoint setSavepoint(String name) throws SQLException { return delegate.setSavepoint(name); }
        @Override public void rollback(Savepoint savepoint) throws SQLException { delegate.rollback(savepoint); }
        @Override public void releaseSavepoint(Savepoint savepoint) throws SQLException { delegate.releaseSavepoint(savepoint); }
        @Override public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability); }
        @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability); }
        @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability); }
        @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { return delegate.prepareStatement(sql, autoGeneratedKeys); }
        @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException { return delegate.prepareStatement(sql, columnIndexes); }
        @Override public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException { return delegate.prepareStatement(sql, columnNames); }
        @Override public Clob createClob() throws SQLException { return delegate.createClob(); }
        @Override public Blob createBlob() throws SQLException { return delegate.createBlob(); }
        @Override public NClob createNClob() throws SQLException { return delegate.createNClob(); }
        @Override public SQLXML createSQLXML() throws SQLException { return delegate.createSQLXML(); }
        @Override public boolean isValid(int timeout) throws SQLException { return delegate.isValid(timeout); }
        @Override public void setClientInfo(String name, String value) throws SQLClientInfoException { delegate.setClientInfo(name, value); }
        @Override public void setClientInfo(Properties properties) throws SQLClientInfoException { delegate.setClientInfo(properties); }
        @Override public String getClientInfo(String name) throws SQLException { return delegate.getClientInfo(name); }
        @Override public Properties getClientInfo() throws SQLException { return delegate.getClientInfo(); }
        @Override public Array createArrayOf(String typeName, Object[] elements) throws SQLException { return delegate.createArrayOf(typeName, elements); }
        @Override public Struct createStruct(String typeName, Object[] attributes) throws SQLException { return delegate.createStruct(typeName, attributes); }
        @Override public void setSchema(String schema) throws SQLException { delegate.setSchema(schema); }
        @Override public String getSchema() throws SQLException { return delegate.getSchema(); }
        @Override public void abort(Executor executor) throws SQLException { delegate.abort(executor); }
        @Override public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException { delegate.setNetworkTimeout(executor, milliseconds); }
        @Override public int getNetworkTimeout() throws SQLException { return delegate.getNetworkTimeout(); }
        @Override public <T> T unwrap(Class<T> iface) throws SQLException { return delegate.unwrap(iface); }
        @Override public boolean isWrapperFor(Class<?> iface) throws SQLException { return delegate.isWrapperFor(iface); }
    }
}