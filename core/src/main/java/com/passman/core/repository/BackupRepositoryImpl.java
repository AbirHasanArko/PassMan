package com.passman.core.repository;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.Backup;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of backup repository
 */
public class BackupRepositoryImpl implements BackupRepository {

    private final DatabaseManager dbManager;

    public BackupRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Backup save(Backup backup) throws RepositoryException {
        String sql = """
            INSERT INTO backups (backup_file_name, backup_path, file_size, checksum, 
                                backup_type, status, description, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager. getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, backup.getBackupFileName());
            stmt.setString(2, backup.getBackupPath());
            stmt.setLong(3, backup.getFileSize());
            stmt.setString(4, backup.getChecksum());
            stmt.setString(5, backup.getBackupType().name());
            stmt.setString(6, backup.getStatus().name());
            stmt.setString(7, backup.getDescription());
            stmt.setObject(8, backup.getCreatedAt());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    backup.setId(rs.getLong(1));
                }
            }

            return backup;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save backup", e);
        }
    }

    @Override
    public Optional<Backup> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM backups WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToBackup(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find backup", e);
        }
    }

    @Override
    public Optional<Backup> findByFileName(String fileName) throws RepositoryException {
        String sql = "SELECT * FROM backups WHERE backup_file_name = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, fileName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs. next()) {
                    return Optional.of(mapResultSetToBackup(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find backup by filename", e);
        }
    }

    @Override
    public List<Backup> findAll() throws RepositoryException {
        String sql = "SELECT * FROM backups ORDER BY created_at DESC";
        List<Backup> backups = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn. createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                backups.add(mapResultSetToBackup(rs));
            }

            return backups;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch backups", e);
        }
    }

    @Override
    public void delete(Long id) throws RepositoryException {
        String sql = "DELETE FROM backups WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt. setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete backup", e);
        }
    }

    private Backup mapResultSetToBackup(ResultSet rs) throws SQLException {
        Backup backup = new Backup();
        backup.setId(rs.getLong("id"));
        backup.setBackupFileName(rs.getString("backup_file_name"));
        backup.setBackupPath(rs.getString("backup_path"));
        backup.setFileSize(rs.getLong("file_size"));
        backup.setChecksum(rs.getString("checksum"));
        backup.setBackupType(Backup.BackupType.valueOf(rs.getString("backup_type")));
        backup.setStatus(Backup.BackupStatus.valueOf(rs.getString("status")));
        backup.setDescription(rs.getString("description"));
        backup.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return backup;
    }
}