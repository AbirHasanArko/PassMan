package com.passman.core.models;

import java.time.LocalDateTime;

/**
 * Model for backup metadata
 */
public class Backup {
    private Long id;
    private String backupFileName;
    private String backupPath;
    private long fileSize;
    private String checksum;
    private BackupType backupType;
    private BackupStatus status;
    private LocalDateTime createdAt;
    private String description;

    public enum BackupType {
        MANUAL, AUTOMATIC, SCHEDULED
    }

    public enum BackupStatus {
        IN_PROGRESS, COMPLETED, FAILED, CORRUPTED
    }

    public Backup() {
        this.createdAt = LocalDateTime. now();
        this.status = BackupStatus. IN_PROGRESS;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBackupFileName() { return backupFileName; }
    public void setBackupFileName(String backupFileName) { this.backupFileName = backupFileName; }

    public String getBackupPath() { return backupPath; }
    public void setBackupPath(String backupPath) { this.backupPath = backupPath; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public String getChecksum() { return checksum; }
    public void setChecksum(String checksum) { this.checksum = checksum; }

    public BackupType getBackupType() { return backupType; }
    public void setBackupType(BackupType backupType) { this.backupType = backupType; }

    public BackupStatus getStatus() { return status; }
    public void setStatus(BackupStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}