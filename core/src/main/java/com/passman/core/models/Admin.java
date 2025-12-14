package com.passman.core.models;

import java.time. LocalDateTime;

/**
 * Domain model for admin accounts.
 */
public class Admin {
    private Long adminId;
    private String username;
    private byte[] hashedPassword;
    private String role; // game_setter, newsletter_publisher, super_admin
    private LocalDateTime lastLogin;
    private String status; // active, inactive

    public Admin() {
    }

    public Admin(String username, byte[] hashedPassword, String role) {
        this.username = username;
        this.hashedPassword = hashedPassword;
        this.role = role;
        this.status = "active";
    }

    // Getters and Setters
    public Long getAdminId() { return adminId; }
    public void setAdminId(Long adminId) { this.adminId = adminId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public byte[] getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(byte[] hashedPassword) { this.hashedPassword = hashedPassword; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this. lastLogin = lastLogin; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this. status = status; }
}