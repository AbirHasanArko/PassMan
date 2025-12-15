package com.passman.core.repository;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.FileVault;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of file vault repository
 */
public class FileVaultRepositoryImpl implements FileVaultRepository {

    private final DatabaseManager dbManager;

    public FileVaultRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public FileVault save(FileVault vault) throws RepositoryException {
        String sql = """
            INSERT INTO file_vaults (vault_name, vault_type, vault_password_hash, 
                                    vault_salt, icon_emoji, has_separate_password, 
                                    is_locked, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement. RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vault.getVaultName());
            stmt.setString(2, vault.getVaultType().name());
            stmt.setBytes(3, vault.getVaultPasswordHash());
            stmt.setBytes(4, vault.getVaultSalt());
            stmt.setString(5, vault.getIconEmoji());
            stmt.setBoolean(6, vault. isHasSeparatePassword());
            stmt.setBoolean(7, vault.isLocked());
            stmt.setObject(8, vault.getCreatedAt());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    vault.setId(rs.getLong(1));
                }
            }

            return vault;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save vault", e);
        }
    }

    @Override
    public Optional<FileVault> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM file_vaults WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVault(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find vault", e);
        }
    }

    @Override
    public Optional<FileVault> findByType(FileVault.VaultType type) throws RepositoryException {
        String sql = "SELECT * FROM file_vaults WHERE vault_type = ? ";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVault(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find vault by type", e);
        }
    }

    @Override
    public List<FileVault> findAll() throws RepositoryException {
        String sql = "SELECT * FROM file_vaults ORDER BY created_at";
        List<FileVault> vaults = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vaults.add(mapResultSetToVault(rs));
            }

            return vaults;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch vaults", e);
        }
    }

    @Override
    public void update(FileVault vault) throws RepositoryException {
        String sql = """
            UPDATE file_vaults 
            SET vault_name = ?, vault_type = ?, vault_password_hash = ?, 
                vault_salt = ?, icon_emoji = ?, has_separate_password = ?, 
                is_locked = ?, last_accessed = ? 
            WHERE id = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vault.getVaultName());
            stmt.setString(2, vault.getVaultType().name());
            stmt.setBytes(3, vault. getVaultPasswordHash());
            stmt.setBytes(4, vault.getVaultSalt());
            stmt.setString(5, vault.getIconEmoji());
            stmt.setBoolean(6, vault.isHasSeparatePassword());
            stmt.setBoolean(7, vault.isLocked());
            stmt.setObject(8, vault.getLastAccessed());
            stmt.setLong(9, vault.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update vault", e);
        }
    }

    @Override
    public void delete(Long id) throws RepositoryException {
        String sql = "DELETE FROM file_vaults WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete vault", e);
        }
    }

    private FileVault mapResultSetToVault(ResultSet rs) throws SQLException {
        FileVault vault = new FileVault();
        vault.setId(rs. getLong("id"));
        vault.setVaultName(rs. getString("vault_name"));
        vault.setVaultType(FileVault.VaultType.valueOf(rs.getString("vault_type")));
        vault.setVaultPasswordHash(rs.getBytes("vault_password_hash"));
        vault.setVaultSalt(rs.getBytes("vault_salt"));
        vault.setIconEmoji(rs.getString("icon_emoji"));

        try {
            vault.setHasSeparatePassword(rs.getBoolean("has_separate_password"));
            vault.setLocked(rs. getBoolean("is_locked"));
            vault.setLastAccessed(rs.getObject("last_accessed", LocalDateTime.class));
        } catch (SQLException e) {
            vault.setHasSeparatePassword(false);
            vault.setLocked(false);
        }

        vault.setCreatedAt(rs. getObject("created_at", LocalDateTime.class));
        return vault;
    }
}