package com. passman.core.models;

import java.time.LocalDateTime;

/**
 * Model for file vaults (categorized storage)
 */
public class FileVault {
    private Long id;
    private String vaultName;
    private VaultType vaultType;
    private String iconEmoji;
    private LocalDateTime createdAt;

    public enum VaultType {
        IMAGES, PDFS, DOCUMENTS, OTHERS
    }

    public FileVault() {
        this.createdAt = LocalDateTime. now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVaultName() { return vaultName; }
    public void setVaultName(String vaultName) { this.vaultName = vaultName; }

    public VaultType getVaultType() { return vaultType; }
    public void setVaultType(VaultType vaultType) { this.vaultType = vaultType; }

    public String getIconEmoji() { return iconEmoji; }
    public void setIconEmoji(String iconEmoji) { this.iconEmoji = iconEmoji; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}