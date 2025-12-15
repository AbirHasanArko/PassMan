package com.passman.core.services;

import com.passman.core.model.Backup;

import javax.crypto.SecretKey;
import java.io.File;
import java.util.List;

/**
 * Service interface for backup operations
 */
public interface BackupService {

    /**
     * Create encrypted backup of entire database
     */
    Backup createBackup(SecretKey masterKey, String description) throws BackupException;

    /**
     * Restore database from backup
     */
    void restoreBackup(File backupFile, SecretKey masterKey) throws BackupException;

    /**
     * Verify backup integrity
     */
    boolean verifyBackup(File backupFile) throws BackupException;

    /**
     * Get all backup metadata
     */
    List<Backup> getAllBackups() throws BackupException;

    /**
     * Delete backup file
     */
    void deleteBackup(Long backupId) throws BackupException;

    /**
     * Get backup statistics
     */
    BackupStatistics getStatistics() throws BackupException;

    class BackupStatistics {
        public int totalBackups;
        public long totalSize;
        public Backup latestBackup;
        public Backup oldestBackup;
    }
}