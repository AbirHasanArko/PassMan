package com.passman.core.db.dao;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.Credential;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object for Credential operations
 */
public class CredentialDAO {

    private final DatabaseManager dbManager;

    public CredentialDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Create a new credential
     */
    public Credential create(Credential credential) throws SQLException {
        String sql = """
            INSERT INTO credentials (title, username, email, url, encrypted_password, 
                                    encryption_iv, notes, tags, is_favorite, created_at, 
                                    last_modified, password_changed_at, password_strength_score, is_breached)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager. getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, credential.getTitle());
            stmt.setString(2, credential.getUsername());
            stmt.setString(3, credential.getEmail());
            stmt.setString(4, credential.getUrl());
            stmt.setBytes(5, credential.getEncryptedPassword());
            stmt.setBytes(6, credential.getEncryptionIV());
            stmt.setString(7, credential.getNotes());
            stmt.setString(8, credential.getTags());
            stmt.setBoolean(9, credential.isFavorite());
            stmt.setObject(10, credential.getCreatedAt());
            stmt.setObject(11, credential.getLastModified());
            stmt.setObject(12, credential.getPasswordChangedAt());
            stmt.setInt(13, credential.getPasswordStrengthScore());
            stmt.setBoolean(14, credential. isBreached());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    credential.setId(rs.getLong(1));
                }
            }

            return credential;
        }
    }

    /**
     * Find credential by ID
     */
    public Optional<Credential> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM credentials WHERE id = ? ";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCredential(rs));
                }
            }

            return Optional.empty();
        }
    }

    /**
     * Find all credentials
     */
    public List<Credential> findAll() throws SQLException {
        String sql = "SELECT * FROM credentials ORDER BY last_modified DESC";
        List<Credential> credentials = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                credentials.add(mapResultSetToCredential(rs));
            }

            return credentials;
        }
    }

    /**
     * Search credentials by title, username, or URL
     */
    public List<Credential> search(String query) throws SQLException {
        String sql = """
            SELECT * FROM credentials 
            WHERE LOWER(title) LIKE LOWER(?) 
               OR LOWER(username) LIKE LOWER(?) 
               OR LOWER(url) LIKE LOWER(?)
            ORDER BY last_modified DESC
            """;

        List<Credential> credentials = new ArrayList<>();
        String searchPattern = "%" + query + "%";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn. prepareStatement(sql)) {

            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    credentials.add(mapResultSetToCredential(rs));
                }
            }

            return credentials;
        }
    }

    /**
     * Find favorite credentials
     */
    public List<Credential> findFavorites() throws SQLException {
        String sql = "SELECT * FROM credentials WHERE is_favorite = 1 ORDER BY title";
        List<Credential> credentials = new ArrayList<>();

        try (Connection conn = dbManager. getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                credentials.add(mapResultSetToCredential(rs));
            }

            return credentials;
        }
    }

    /**
     * Update credential
     */
    public void update(Credential credential) throws SQLException {
        String sql = """
            UPDATE credentials 
            SET title = ?, username = ?, email = ?, url = ?, 
                encrypted_password = ?, encryption_iv = ?, notes = ?, 
                tags = ?, is_favorite = ?, last_modified = ?,
                password_changed_at = ?, password_strength_score = ?, is_breached = ? 
            WHERE id = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, credential.getTitle());
            stmt.setString(2, credential.getUsername());
            stmt.setString(3, credential.getEmail());
            stmt.setString(4, credential. getUrl());
            stmt.setBytes(5, credential.getEncryptedPassword());
            stmt.setBytes(6, credential.getEncryptionIV());
            stmt.setString(7, credential.getNotes());
            stmt.setString(8, credential.getTags());
            stmt.setBoolean(9, credential.isFavorite());
            stmt.setObject(10, LocalDateTime.now());
            stmt.setObject(11, credential.getPasswordChangedAt());
            stmt.setInt(12, credential.getPasswordStrengthScore());
            stmt.setBoolean(13, credential.isBreached());
            stmt.setLong(14, credential.getId());

            stmt.executeUpdate();
        }
    }

    /**
     * Delete credential
     */
    public void delete(Long id) throws SQLException {
        String sql = "DELETE FROM credentials WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        }
    }

    /**
     * Count total credentials
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM credentials";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn. createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    private Credential mapResultSetToCredential(ResultSet rs) throws SQLException {
        Credential credential = new Credential();
        credential.setId(rs.getLong("id"));
        credential.setTitle(rs.getString("title"));
        credential.setUsername(rs.getString("username"));
        credential.setEmail(rs.getString("email"));
        credential.setUrl(rs. getString("url"));
        credential. setEncryptedPassword(rs. getBytes("encrypted_password"));
        credential.setEncryptionIV(rs.getBytes("encryption_iv"));
        credential.setNotes(rs.getString("notes"));
        credential.setTags(rs.getString("tags"));
        credential.setFavorite(rs.getBoolean("is_favorite"));
        credential.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        credential.setLastModified(rs.getObject("last_modified", LocalDateTime.class));
        credential.setPasswordChangedAt(rs.getObject("password_changed_at", LocalDateTime. class));
        credential.setPasswordStrengthScore(rs.getInt("password_strength_score"));
        credential.setBreached(rs.getBoolean("is_breached"));
        return credential;
    }
}