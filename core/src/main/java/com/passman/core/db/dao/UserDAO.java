package com.passman.core.db. dao;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Data Access Object for User operations
 */
public class UserDAO {

    private final DatabaseManager dbManager;

    public UserDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Create a new user
     */
    public User create(User user) throws SQLException {
        String sql = "INSERT INTO users (username, salt, hashed_password, created_at) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setBytes(2, user.getSalt());
            stmt.setBytes(3, user.getHashedPassword());
            stmt.setObject(4, user.getCreatedAt());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getLong(1));
                }
            }

            return user;
        }
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUser(rs));
                }
            }

            return Optional.empty();
        }
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) throws SQLException {
        String sql = "SELECT * FROM users WHERE id = ?";

        try (Connection conn = dbManager. getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional. of(mapResultSetToUser(rs));
                }
            }

            return Optional.empty();
        }
    }

    /**
     * Update last login time
     */
    public void updateLastLogin(Long userId, LocalDateTime lastLogin) throws SQLException {
        String sql = "UPDATE users SET last_login = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt. setObject(1, lastLogin);
            stmt.setLong(2, userId);

            stmt.executeUpdate();
        }
    }

    /**
     * Check if any user exists
     */
    public boolean userExists() throws SQLException {
        String sql = "SELECT COUNT(*) FROM users";

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt. executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    /**
     * Delete user
     */
    public void delete(Long userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.executeUpdate();
        }
    }

    /**
     * Update user password
     */
    public void updatePassword(Long userId, byte[] salt, byte[] hashedPassword) throws SQLException {
        String sql = "UPDATE users SET salt = ?, hashed_password = ? WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBytes(1, salt);
            stmt.setBytes(2, hashedPassword);
            stmt.setLong(3, userId);

            stmt.executeUpdate();
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setSalt(rs.getBytes("salt"));
        user.setHashedPassword(rs.getBytes("hashed_password"));
        user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        user.setLastLogin(rs.getObject("last_login", LocalDateTime.class));
        return user;
    }
}