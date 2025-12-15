package com.passman.core.model;

import java.time.LocalDateTime;

/**
 * Model for file vaults with optional separate passwords
 */
public class FileVault {
    private Long id;
    private String vaultName;
    private VaultType vaultType;
    private byte[] vaultPasswordHash;
    private byte[] vaultSalt;
    private String iconEmoji;
    private boolean hasSeparatePassword;
    private boolean isLocked;
    private LocalDateTime createdAt;
    private LocalDateTime lastAccessed;

    public enum VaultType {
        IMAGES, PDFS, DOCUMENTS, OTHERS, CUSTOM
    }

    public FileVault() {
        this.createdAt = LocalDateTime. now();
        this.hasSeparatePassword = false;
        this.isLocked = true;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVaultName() { return vaultName; }
    public void setVaultName(String vaultName) { this.vaultName = vaultName; }

    public VaultType getVaultType() { return vaultType; }
    public void setVaultType(VaultType vaultType) { this.vaultType = vaultType; }

    public byte[] getVaultPasswordHash() { return vaultPasswordHash; }
    public void setVaultPasswordHash(byte[] vaultPasswordHash) { this.vaultPasswordHash = vaultPasswordHash; }

    public byte[] getVaultSalt() { return vaultSalt; }
    public void setVaultSalt(byte[] vaultSalt) { this.vaultSalt = vaultSalt; }

    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }

    public boolean isHasSeparatePassword() { return hasSeparatePassword; }
    public void setHasSeparatePassword(boolean hasSeparatePassword) { this.hasSeparatePassword = hasSeparatePassword; }

    public boolean isLocked() { return isLocked; }
    public void setLocked(boolean locked) { isLocked = locked; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
}