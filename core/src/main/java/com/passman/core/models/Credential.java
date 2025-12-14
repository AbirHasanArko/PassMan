package com.passman.core.models;

import java.time.LocalDateTime;

/**
 * Domain model for password credential entries.
 */
public class Credential {
    private Long id;
    private String title;
    private String username;
    private String email;
    private String url;
    private byte[] encryptedPassword;
    private byte[] encryptionIV;
    private String notes;
    private String tags;
    private boolean isFavorite;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;

    public Credential() {
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime. now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public byte[] getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(byte[] encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public byte[] getEncryptionIV() { return encryptionIV; }
    public void setEncryptionIV(byte[] encryptionIV) { this.encryptionIV = encryptionIV; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }
}