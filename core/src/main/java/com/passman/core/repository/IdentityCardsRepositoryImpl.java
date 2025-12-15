package com.passman.core.repository;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.IdentityCard;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQLite implementation of identity cards repository
 */
public class IdentityCardsRepositoryImpl implements IdentityCardsRepository {

    private final DatabaseManager dbManager;

    public IdentityCardsRepositoryImpl(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public IdentityCard save(IdentityCard card) throws RepositoryException {
        String sql = """
            INSERT INTO identity_cards (card_type, card_name, encrypted_data, encryption_iv, 
                                       card_number_last4, issuing_country, issuing_authority, 
                                       issue_date, expiry_date, has_photo, encrypted_photo, 
                                       photo_encryption_iv, is_expired, tags, color_code, 
                                       created_at, last_modified)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, card. getCardType().name());
            stmt.setString(2, card.getCardName());
            stmt.setBytes(3, card. getEncryptedData());
            stmt.setBytes(4, card.getEncryptionIV());
            stmt.setString(5, card.getCardNumberLast4());
            stmt.setString(6, card.getIssuingCountry());
            stmt.setString(7, card. getIssuingAuthority());
            stmt.setObject(8, card.getIssueDate());
            stmt.setObject(9, card.getExpiryDate());
            stmt.setBoolean(10, card.isHasPhoto());
            stmt.setBytes(11, card.getEncryptedPhoto());
            stmt.setBytes(12, card.getPhotoEncryptionIV());
            stmt.setBoolean(13, card.isExpired());
            stmt.setString(14, card.getTags());
            stmt.setString(15, card.getColorCode());
            stmt.setObject(16, card.getCreatedAt());
            stmt.setObject(17, card.getLastModified());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    card.setId(rs.getLong(1));
                }
            }

            return card;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to save identity card", e);
        }
    }

    @Override
    public Optional<IdentityCard> findById(Long id) throws RepositoryException {
        String sql = "SELECT * FROM identity_cards WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt. setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCard(rs));
                }
            }

            return Optional.empty();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find identity card", e);
        }
    }

    @Override
    public List<IdentityCard> findAll() throws RepositoryException {
        String sql = "SELECT * FROM identity_cards ORDER BY last_modified DESC";
        List<IdentityCard> cards = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn. createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                cards. add(mapResultSetToCard(rs));
            }

            return cards;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch identity cards", e);
        }
    }

    @Override
    public List<IdentityCard> findByType(IdentityCard.CardType type) throws RepositoryException {
        String sql = "SELECT * FROM identity_cards WHERE card_type = ?  ORDER BY last_modified DESC";
        List<IdentityCard> cards = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, type.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToCard(rs));
                }
            }

            return cards;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find cards by type", e);
        }
    }

    @Override
    public List<IdentityCard> findExpiringBefore(LocalDate date) throws RepositoryException {
        String sql = "SELECT * FROM identity_cards WHERE expiry_date <= ? AND expiry_date >= ?  ORDER BY expiry_date";
        List<IdentityCard> cards = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, date);
            stmt.setObject(2, LocalDate.now());

            try (ResultSet rs = stmt. executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToCard(rs));
                }
            }

            return cards;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to find expiring cards", e);
        }
    }

    @Override
    public List<IdentityCard> findExpired() throws RepositoryException {
        String sql = "SELECT * FROM identity_cards WHERE is_expired = 1 ORDER BY expiry_date DESC";
        List<IdentityCard> cards = new ArrayList<>();

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt. executeQuery(sql)) {

            while (rs.next()) {
                cards.add(mapResultSetToCard(rs));
            }

            return cards;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to fetch expired cards", e);
        }
    }

    @Override
    public List<IdentityCard> search(String query) throws RepositoryException {
        String sql = """
            SELECT * FROM identity_cards 
            WHERE LOWER(card_name) LIKE LOWER(?) 
               OR LOWER(card_number_last4) LIKE LOWER(?) 
               OR LOWER(tags) LIKE LOWER(?)
            ORDER BY last_modified DESC
            """;
        List<IdentityCard> cards = new ArrayList<>();
        String searchPattern = "%" + query + "%";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn. prepareStatement(sql)) {

            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cards.add(mapResultSetToCard(rs));
                }
            }

            return cards;
        } catch (SQLException e) {
            throw new RepositoryException("Failed to search identity cards", e);
        }
    }

    @Override
    public void update(IdentityCard card) throws RepositoryException {
        String sql = """
            UPDATE identity_cards 
            SET card_type = ?, card_name = ?, encrypted_data = ?, encryption_iv = ?,
                card_number_last4 = ?, issuing_country = ?, issuing_authority = ?,
                issue_date = ?, expiry_date = ?, has_photo = ?, encrypted_photo = ?,
                photo_encryption_iv = ?, is_expired = ?, tags = ?, color_code = ?,
                last_modified = ? 
            WHERE id = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, card.getCardType().name());
            stmt.setString(2, card.getCardName());
            stmt.setBytes(3, card.getEncryptedData());
            stmt.setBytes(4, card.getEncryptionIV());
            stmt.setString(5, card.getCardNumberLast4());
            stmt.setString(6, card.getIssuingCountry());
            stmt.setString(7, card.getIssuingAuthority());
            stmt.setObject(8, card.getIssueDate());
            stmt.setObject(9, card. getExpiryDate());
            stmt.setBoolean(10, card.isHasPhoto());
            stmt.setBytes(11, card.getEncryptedPhoto());
            stmt.setBytes(12, card.getPhotoEncryptionIV());
            stmt.setBoolean(13, card. isExpired());
            stmt.setString(14, card.getTags());
            stmt.setString(15, card.getColorCode());
            stmt.setObject(16, LocalDateTime.now());
            stmt.setLong(17, card. getId());

            stmt. executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to update identity card", e);
        }
    }

    @Override
    public void delete(Long id) throws RepositoryException {
        String sql = "DELETE FROM identity_cards WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn. prepareStatement(sql)) {

            stmt.setLong(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RepositoryException("Failed to delete identity card", e);
        }
    }

    private IdentityCard mapResultSetToCard(ResultSet rs) throws SQLException {
        IdentityCard card = new IdentityCard();
        card.setId(rs.getLong("id"));
        card.setCardType(IdentityCard.CardType. valueOf(rs.getString("card_type")));
        card.setCardName(rs.getString("card_name"));
        card.setEncryptedData(rs.getBytes("encrypted_data"));
        card.setEncryptionIV(rs.getBytes("encryption_iv"));
        card.setCardNumberLast4(rs.getString("card_number_last4"));
        card.setIssuingCountry(rs.getString("issuing_country"));
        card.setIssuingAuthority(rs.getString("issuing_authority"));
        card.setIssueDate(rs.getObject("issue_date", LocalDate.class));
        card.setExpiryDate(rs.getObject("expiry_date", LocalDate.class));
        card.setHasPhoto(rs.getBoolean("has_photo"));
        card.setEncryptedPhoto(rs.getBytes("encrypted_photo"));
        card.setPhotoEncryptionIV(rs.getBytes("photo_encryption_iv"));
        card.setExpired(rs.getBoolean("is_expired"));
        card.setTags(rs.getString("tags"));
        card.setColorCode(rs.getString("color_code"));
        card.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        card.setLastModified(rs.getObject("last_modified", LocalDateTime.class));
        return card;
    }
}