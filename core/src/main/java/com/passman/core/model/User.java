package com.passman.core.model;

import java.time.LocalDateTime;

/**
 * Domain model for master user
 */
public class User {
    private Long id;
    private String username;
    private byte[] salt;
    private byte[] hashedPassword;
    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

    public User() {
    }

    public User(String username, byte[] salt, byte[] hashedPassword) {
        this.username = username;
        this. salt = salt;
        this. hashedPassword = hashedPassword;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public byte[] getSalt() { return salt; }
    public void setSalt(byte[] salt) { this.salt = salt; }

    public byte[] getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(byte[] hashedPassword) { this.hashedPassword = hashedPassword; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }
}