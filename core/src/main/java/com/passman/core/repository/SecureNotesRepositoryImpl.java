package com.passman.core.repository;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.NoteAttachment;
import com.passman.core.model.SecureNote;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of secure notes repository
 */
public class SecureNotesRepositoryImpl implements SecureNotesRepository {

    private final DatabaseManager dbManager;

    public SecureNotesRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public SecureNote save(SecureNote note) throws RepositoryException {
        String sql = """
            INSERT INTO secure_notes (title, encrypted_content, encryption_iv, category, 
                                     tags, is_favorite, has_attachments, color_code, 
                                     created_at, last_modified)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager. getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, note.getTitle());
            stmt.setBytes(2, note.getEncryptedContent());
            stmt.setBytes(3, note.getEncryptionIV());
            stmt.setString(4, note.getCategory().name());
            stmt.setString(5, note.getTags());
            stmt.setBoolean(6, note.isFavorite());
            stmt.setBoolean(7, note.isHasAttachments());
            stmt.setString(8, note.getColorCode());
            stmt.setObject(9, note. getCreatedAt());
            stmt.setObject(10, note. getLastModified());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    note.setId(rs.getLong(1));
                }
            }

            return note;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save note", e);
        }
    }

    @Override
    public Optional<SecureNote> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM secure_notes WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs. next()) {
                    return Optional.of(mapResultSetToNote(rs));
                }
            }

            return Optional. empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find note", e);
        }
    }

    @Override
    public List<SecureNote> findAll() throws RepositoryException {
        String sql = "SELECT * FROM secure_notes ORDER BY last_modified DESC";
        List<SecureNote> notes = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs. next()) {
                notes.add(mapResultSetToNote(rs));
            }

            return notes;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch notes", e);
        }
    }

    @Override
    public List<SecureNote> searchByTitle(String query) throws RepositoryException {
        String sql = "SELECT * FROM secure_notes WHERE LOWER(title) LIKE LOWER(?) OR LOWER(tags) LIKE LOWER(?) ORDER BY last_modified DESC";
        List<SecureNote> notes = new ArrayList<>();
        String searchPattern = "%" + query + "%";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs. next()) {
                    notes. add(mapResultSetToNote(rs));
                }
            }

            return notes;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to search notes", e);
        }
    }

    @Override
    public List<SecureNote> findByCategory(SecureNote.NoteCategory category) throws RepositoryException {
        String sql = "SELECT * FROM secure_notes WHERE category = ? ORDER BY last_modified DESC";
        List<SecureNote> notes = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category.name());

            try (ResultSet rs = stmt. executeQuery()) {
                while (rs.next()) {
                    notes.add(mapResultSetToNote(rs));
                }
            }

            return notes;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find notes by category", e);
        }
    }

    @Override
    public List<SecureNote> findFavorites() throws RepositoryException {
        String sql = "SELECT * FROM secure_notes WHERE is_favorite = 1 ORDER BY last_modified DESC";
        List<SecureNote> notes = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs. next()) {
                notes.add(mapResultSetToNote(rs));
            }

            return notes;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch favorite notes", e);
        }
    }

    @Override
    public void update(SecureNote note) throws RepositoryException {
        String sql = """
            UPDATE secure_notes 
            SET title = ?, encrypted_content = ?, encryption_iv = ?, category = ?, 
                tags = ?, is_favorite = ?, has_attachments = ?, color_code = ?, 
                last_modified = ?  
            WHERE id = ? 
            """;

        try (Connection conn = dbManager. getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, note. getTitle());
            stmt.setBytes(2, note.getEncryptedContent());
            stmt.setBytes(3, note.getEncryptionIV());
            stmt.setString(4, note.getCategory().name());
            stmt.setString(5, note.getTags());
            stmt.setBoolean(6, note. isFavorite());
            stmt.setBoolean(7, note.isHasAttachments());
            stmt.setString(8, note.getColorCode());
            stmt.setObject(9, LocalDateTime.now());
            stmt.setLong(10, note. getId());

            stmt. executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update note", e);
        }
    }

    @Override
    public void delete(Long id) throws RepositoryException {
        String sql = "DELETE FROM secure_notes WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete note", e);
        }
    }

    @Override
    public NoteAttachment saveAttachment(NoteAttachment attachment) throws RepositoryException {
        String sql = """
            INSERT INTO note_attachments (note_id, original_file_name, encrypted_file_name, 
                                         file_size, mime_type, checksum, uploaded_at)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, attachment.getNoteId());
            stmt.setString(2, attachment.getOriginalFileName());
            stmt.setString(3, attachment.getEncryptedFileName());
            stmt.setLong(4, attachment.getFileSize());
            stmt.setString(5, attachment.getMimeType());
            stmt.setString(6, attachment.getChecksum());
            stmt.setObject(7, attachment.getUploadedAt());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    attachment.setId(rs.getLong(1));
                }
            }

            return attachment;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save attachment", e);
        }
    }

    @Override
    public Optional<NoteAttachment> findAttachmentById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM note_attachments WHERE id = ? ";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAttachment(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find attachment", e);
        }
    }

    @Override
    public List<NoteAttachment> findAttachmentsByNoteId(Long noteId) throws RepositoryException {
        String sql = "SELECT * FROM note_attachments WHERE note_id = ? ORDER BY uploaded_at";
        List<NoteAttachment> attachments = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, noteId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    attachments.add(mapResultSetToAttachment(rs));
                }
            }

            return attachments;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch attachments", e);
        }
    }

    @Override
    public void deleteAttachment(Long id) throws RepositoryException {
        String sql = "DELETE FROM note_attachments WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn. prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete attachment", e);
        }
    }

    private SecureNote mapResultSetToNote(ResultSet rs) throws SQLException {
        SecureNote note = new SecureNote();
        note.setId(rs.getLong("id"));
        note.setTitle(rs.getString("title"));
        note.setEncryptedContent(rs.getBytes("encrypted_content"));
        note.setEncryptionIV(rs.getBytes("encryption_iv"));
        note.setCategory(SecureNote.NoteCategory. valueOf(rs.getString("category")));
        note.setTags(rs.getString("tags"));
        note.setFavorite(rs.getBoolean("is_favorite"));
        note.setHasAttachments(rs.getBoolean("has_attachments"));
        note.setColorCode(rs.getString("color_code"));
        note.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        note.setLastModified(rs.getObject("last_modified", LocalDateTime.class));
        return note;
    }

    private NoteAttachment mapResultSetToAttachment(ResultSet rs) throws SQLException {
        NoteAttachment attachment = new NoteAttachment();
        attachment.setId(rs.getLong("id"));
        attachment.setNoteId(rs.getLong("note_id"));
        attachment.setOriginalFileName(rs.getString("original_file_name"));
        attachment.setEncryptedFileName(rs.getString("encrypted_file_name"));
        attachment.setFileSize(rs.getLong("file_size"));
        attachment.setMimeType(rs.getString("mime_type"));
        attachment. setChecksum(rs.getString("checksum"));
        attachment.setUploadedAt(rs.getObject("uploaded_at", LocalDateTime.class));
        return attachment;
    }
}