package com.passman.android.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity for storing master user authentication data
 */
@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "username")
    private String username;

    @ColumnInfo(name = "salt", typeAffinity = ColumnInfo.BLOB)
    private byte[] salt;

    @ColumnInfo(name = "hashed_password", typeAffinity = ColumnInfo.BLOB)
    private byte[] hashedPassword;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "last_login")
    private long lastLogin;

    @ColumnInfo(name = "biometric_enabled")
    private boolean biometricEnabled;

    @ColumnInfo(name = "auto_lock_timeout")
    private int autoLockTimeout; // in minutes

    @ColumnInfo(name = "clipboard_timeout")
    private int clipboardTimeout; // in seconds

    @ColumnInfo(name = "theme_mode")
    private String themeMode; // "system", "light", "dark"

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public byte[] getSalt() { return salt; }
    public void setSalt(byte[] salt) { this.salt = salt; }

    public byte[] getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(byte[] hashedPassword) { this.hashedPassword = hashedPassword; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastLogin() { return lastLogin; }
    public void setLastLogin(long lastLogin) { this.lastLogin = lastLogin; }

    public boolean isBiometricEnabled() { return biometricEnabled; }
    public void setBiometricEnabled(boolean biometricEnabled) { this.biometricEnabled = biometricEnabled; }

    public int getAutoLockTimeout() { return autoLockTimeout; }
    public void setAutoLockTimeout(int autoLockTimeout) { this.autoLockTimeout = autoLockTimeout; }

    public int getClipboardTimeout() { return clipboardTimeout; }
    public void setClipboardTimeout(int clipboardTimeout) { this.clipboardTimeout = clipboardTimeout; }

    public String getThemeMode() { return themeMode; }
    public void setThemeMode(String themeMode) { this.themeMode = themeMode; }
}
