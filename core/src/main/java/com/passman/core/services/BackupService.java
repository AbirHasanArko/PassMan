package com.passman.core.services;

import com.passman.core.model.Backup;

import javax.crypto.SecretKey;
import java.io.File;
import java.util.List;

/**
 * Service for creating and restoring encrypted backups
 */
public interface BackupService {

    /**
     * Creates encrypted backup of all data
     */
    Backup createBackup(File destinationFile, SecretKey masterKey, String description)
            throws BackupException;

    /**
     * Restores data from encrypted backup
     */
    void restoreBackup(File backupFile, SecretKey masterKey) throws BackupException;

    /**
     * Verifies backup integrity
     */
    boolean verifyBackup(File backupFile) throws BackupException;

    /**
     * Lists all available backups
     */
    List<Backup> listBackups() throws BackupException;

    /**
     * Deletes a backup
     */
    void deleteBackup(Long backupId) throws BackupException;

    /**
     * Gets backup metadata
     */
    Backup getBackupInfo(File backupFile) throws BackupException;
}