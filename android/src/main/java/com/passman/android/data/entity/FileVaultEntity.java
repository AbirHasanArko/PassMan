package com.passman.android.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity representing a file vault
 */
@Entity(tableName = "file_vaults")
public class FileVaultEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "vault_name")
    private String vaultName;

    @ColumnInfo(name = "vault_type")
    private String vaultType;

    @ColumnInfo(name = "icon_emoji")
    private String iconEmoji;

    @ColumnInfo(name = "has_separate_password")
    private boolean hasSeparatePassword;

    @ColumnInfo(name = "is_locked")
    private boolean isLocked;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "last_accessed")
    private long lastAccessed;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getVaultName() { return vaultName; }
    public void setVaultName(String vaultName) { this.vaultName = vaultName; }

    public String getVaultType() { return vaultType; }
    public void setVaultType(String vaultType) { this.vaultType = vaultType; }

    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }

    public boolean isHasSeparatePassword() { return hasSeparatePassword; }
    public void setHasSeparatePassword(boolean hasSeparatePassword) { this.hasSeparatePassword = hasSeparatePassword; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(long lastAccessed) { this.lastAccessed = lastAccessed; }
}
