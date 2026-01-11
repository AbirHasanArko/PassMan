package com.passman.android.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room Entity for storing encrypted credentials
 */
@Entity(
    tableName = "credentials",
    indices = {
        @Index(value = "title"),
        @Index(value = "is_favorite"),
        @Index(value = "created_at")
    }
)
public class CredentialEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "title")
    private String title;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "email")
    private String email;

    @ColumnInfo(name = "url")
    private String url;

    @ColumnInfo(name = "encrypted_password", typeAffinity = ColumnInfo.BLOB)
    private byte[] encryptedPassword;

    @ColumnInfo(name = "encryption_iv", typeAffinity = ColumnInfo.BLOB)
    private byte[] encryptionIV;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "tags")
    private String tags;

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "last_modified")
    private long lastModified;

    @ColumnInfo(name = "password_changed_at")
    private long passwordChangedAt;

    @ColumnInfo(name = "password_strength_score")
    private int passwordStrengthScore;

    @ColumnInfo(name = "is_breached")
    private boolean isBreached;

    @ColumnInfo(name = "category")
    private String category;

    @ColumnInfo(name = "icon_name")
    private String iconName;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

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

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastModified() { return lastModified; }
    public void setLastModified(long lastModified) { this.lastModified = lastModified; }

    public long getPasswordChangedAt() { return passwordChangedAt; }
    public void setPasswordChangedAt(long passwordChangedAt) { this.passwordChangedAt = passwordChangedAt; }

    public int getPasswordStrengthScore() { return passwordStrengthScore; }
    public void setPasswordStrengthScore(int passwordStrengthScore) { this.passwordStrengthScore = passwordStrengthScore; }

    public boolean isBreached() { return isBreached; }
    public void setBreached(boolean breached) { isBreached = breached; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getIconName() { return iconName; }
    public void setIconName(String iconName) { this.iconName = iconName; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CredentialEntity that = (CredentialEntity) o;
        return id == that.id &&
               isFavorite == that.isFavorite &&
               lastModified == that.lastModified &&
               java.util.Objects.equals(title, that.title);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(id, title, isFavorite, lastModified);
    }
}
