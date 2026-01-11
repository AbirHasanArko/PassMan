package com.passman.android.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * Room Entity representing an encrypted file within a vault
 */
@Entity(
    tableName = "encrypted_files",
    foreignKeys = @ForeignKey(
        entity = FileVaultEntity.class,
        parentColumns = "id",
        childColumns = "vault_id",
        onDelete = ForeignKey.CASCADE
    ),
    indices = @Index("vault_id")
)
public class EncryptedFileEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "vault_id")
    private long vaultId;

    @ColumnInfo(name = "original_file_name")
    private String originalFileName;

    @ColumnInfo(name = "encrypted_file_name")
    private String encryptedFileName;

    @ColumnInfo(name = "mime_type")
    private String mimeType;

    @ColumnInfo(name = "original_size")
    private long originalSize;

    @ColumnInfo(name = "encrypted_size")
    private long encryptedSize;

    @ColumnInfo(name = "checksum")
    private String checksum;

    @ColumnInfo(name = "uploaded_at")
    private long uploadedAt;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getVaultId() { return vaultId; }
    public void setVaultId(long vaultId) { this.vaultId = vaultId; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public String getEncryptedFileName() { return encryptedFileName; }
    public void setEncryptedFileName(String encryptedFileName) { this.encryptedFileName = encryptedFileName; }

    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }

    public long getOriginalSize() { return originalSize; }
    public void setOriginalSize(long originalSize) { this.originalSize = originalSize; }

    public long getEncryptedSize() { return encryptedSize; }
    public void setEncryptedSize(long encryptedSize) { this.encryptedSize = encryptedSize; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public long getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(long uploadedAt) { this.uploadedAt = uploadedAt; }
}
