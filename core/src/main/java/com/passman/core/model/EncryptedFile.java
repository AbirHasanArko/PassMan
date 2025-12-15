package com.passman.core.model;

import java.time.LocalDateTime;

/**
 * Model for encrypted file entries
 */
public class EncryptedFile {
    private Long id;
    private Long vaultId;
    private String originalFileName;
    private String encryptedFileName;
    private long originalSize;
    private long encryptedSize;
    private String mimeType;
    private byte[] encryptionIV;
    private String checksum;
    private LocalDateTime uploadedAt;
    private LocalDateTime lastAccessed;

    public EncryptedFile() {
        this.uploadedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVaultId() { return vaultId; }
    public void setVaultId(Long vaultId) { this.vaultId = vaultId; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getEncryptedFileName() { return encryptedFileName; }
    public void setEncryptedFileName(String encryptedFileName) { this.encryptedFileName = encryptedFileName; }

    public long getOriginalSize() { return originalSize; }
    public void setOriginalSize(long originalSize) { this.originalSize = originalSize; }

    public long getEncryptedSize() { return encryptedSize; }
    public void setEncryptedSize(long encryptedSize) { this.encryptedSize = encryptedSize; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public byte[] getEncryptionIV() { return encryptionIV; }
    public void setEncryptionIV(byte[] encryptionIV) { this.encryptionIV = encryptionIV; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }

    public LocalDateTime getLastAccessed() { return lastAccessed; }
    public void setLastAccessed(LocalDateTime lastAccessed) { this.lastAccessed = lastAccessed; }
}