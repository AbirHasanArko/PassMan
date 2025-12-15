package com.passman.core.services;

import com.passman.core.crypto.AESCipher;
import com.passman.core.crypto. CipherFactory;
import com. passman.core.db.DatabaseManager;
import com.passman. core.model.Backup;
import com.passman.core.repository.BackupRepository;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file. Files;
import java.nio. file.Path;
import java. nio.file.StandardCopyOption;
import java. security.MessageDigest;
import java.time.LocalDateTime;
import java.util. Base64;
import java.util. List;
import java.util. zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Implementation of backup service with encryption
 */
public class BackupServiceImpl implements BackupService {

    private static final String BACKUP_EXTENSION = ".pmbak";
    private static final String METADATA_FILE = "backup_metadata.json";

    private final DatabaseManager dbManager;
    private final BackupRepository backupRepository;
    private final AESCipher aesCipher;

    public BackupServiceImpl(DatabaseManager dbManager, BackupRepository backupRepository) {
        this.dbManager = dbManager;
        this.backupRepository = backupRepository;
        this.aesCipher = CipherFactory. createAESCipher();
    }

    @Override
    public Backup createBackup(File destinationFile, SecretKey masterKey, String description)
            throws BackupException {
        try {
            // Create backup metadata
            Backup backup = new Backup();
            backup.setBackupFileName(destinationFile.getName());
            backup.setBackupPath(destinationFile.getAbsolutePath());
            backup. setBackupType(Backup.BackupType.MANUAL);
            backup.setDescription(description);
            backup.setCreatedAt(LocalDateTime.now());

            // Create temporary directory for backup staging
            Path tempDir = Files.createTempDirectory("passman_backup_");

            try {
                // Export database to temp directory
                File dbFile = new File(dbManager.getDatabasePath());
                File tempDbFile = tempDir.resolve("passman. db").toFile();
                Files.copy(dbFile.toPath(), tempDbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Create ZIP archive
                File tempZipFile = Files.createTempFile("backup_", ".zip").toFile();
                createZipArchive(tempDir. toFile(), tempZipFile);

                // Read ZIP data
                byte[] zipData = Files.readAllBytes(tempZipFile.toPath());

                // Encrypt backup
                byte[] encryptedData = aesCipher.encryptBytes(zipData, masterKey);

                // Calculate checksum
                String checksum = calculateSHA256(encryptedData);
                backup.setChecksum(checksum);

                // Write encrypted backup
                Files.write(destinationFile.toPath(), encryptedData);
                backup.setFileSize(encryptedData.length);

                // Update status
                backup.setStatus(Backup.BackupStatus. COMPLETED);

                // Save backup metadata
                backupRepository.save(backup);

                // Cleanup
                tempZipFile.delete();
                deleteDirectory(tempDir. toFile());

                return backup;

            } catch (Exception e) {
                backup.setStatus(Backup.BackupStatus.FAILED);
                throw new BackupException("Failed to create backup", e);
            }

        } catch (Exception e) {
            throw new BackupException("Backup process failed", e);
        }
    }

    @Override
    public void restoreBackup(File backupFile, SecretKey masterKey) throws BackupException {
        try {
            // Verify backup first
            if (!verifyBackup(backupFile)) {
                throw new BackupException("Backup verification failed");
            }

            // Read encrypted backup
            byte[] encryptedData = Files.readAllBytes(backupFile.toPath());

            // Decrypt backup
            byte[] zipData = aesCipher. decryptBytes(encryptedData, masterKey);

            // Extract to temporary directory
            Path tempDir = Files.createTempDirectory("passman_restore_");
            File tempZipFile = Files.createTempFile("restore_", ".zip").toFile();
            Files.write(tempZipFile. toPath(), zipData);

            try {
                // Extract ZIP
                extractZipArchive(tempZipFile, tempDir. toFile());

                // Close existing database connection
                dbManager.close();

                // Restore database
                File restoredDb = tempDir.resolve("passman.db").toFile();
                File targetDb = new File(dbManager.getDatabasePath());

                // Backup current database before restore
                File currentBackup = new File(targetDb.getAbsolutePath() + ".before_restore");
                Files.copy(targetDb.toPath(), currentBackup.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Restore database
                Files.copy(restoredDb. toPath(), targetDb.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Reinitialize database connection
                dbManager.initialize();

                // Cleanup
                tempZipFile.delete();
                deleteDirectory(tempDir.toFile());

            } catch (Exception e) {
                throw new BackupException("Failed to restore backup", e);
            }

        } catch (Exception e) {
            throw new BackupException("Restore process failed", e);
        }
    }

    @Override
    public boolean verifyBackup(File backupFile) throws BackupException {
        try {
            if (!backupFile.exists()) {
                return false;
            }

            // Read file
            byte[] fileData = Files. readAllBytes(backupFile. toPath());

            // Calculate checksum
            String actualChecksum = calculateSHA256(fileData);

            // Find backup metadata
            Backup backup = backupRepository.findByFileName(backupFile.getName())
                    .orElse(null);

            if (backup == null) {
                // If no metadata, just check if file is readable
                return fileData.length > 0;
            }

            // Verify checksum
            return actualChecksum.equals(backup.getChecksum());

        } catch (Exception e) {
            throw new BackupException("Verification failed", e);
        }
    }

    @Override
    public List<Backup> listBackups() throws BackupException {
        try {
            return backupRepository.findAll();
        } catch (Exception e) {
            throw new BackupException("Failed to list backups", e);
        }
    }

    @Override
    public void deleteBackup(Long backupId) throws BackupException {
        try {
            Backup backup = backupRepository.findById(backupId)
                    .orElseThrow(() -> new BackupException("Backup not found"));

            // Delete file
            File backupFile = new File(backup. getBackupPath());
            if (backupFile.exists()) {
                Files.delete(backupFile.toPath());
            }

            // Delete metadata
            backupRepository.delete(backupId);

        } catch (Exception e) {
            throw new BackupException("Failed to delete backup", e);
        }
    }

    @Override
    public Backup getBackupInfo(File backupFile) throws BackupException {
        try {
            Backup backup = new Backup();
            backup.setBackupFileName(backupFile.getName());
            backup.setBackupPath(backupFile.getAbsolutePath());
            backup. setFileSize(backupFile.length());

            byte[] fileData = Files.readAllBytes(backupFile.toPath());
            backup.setChecksum(calculateSHA256(fileData));

            return backup;
        } catch (Exception e) {
            throw new BackupException("Failed to get backup info", e);
        }
    }

    // Helper methods

    private void createZipArchive(File sourceDir, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {

            zipDirectory(sourceDir, sourceDir, zos);
        }
    }

    private void zipDirectory(File rootDir, File sourceDir, ZipOutputStream zos) throws IOException {
        File[] files = sourceDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                zipDirectory(rootDir, file, zos);
            } else {
                String relativePath = rootDir.toPath().relativize(file.toPath()).toString();
                ZipEntry entry = new ZipEntry(relativePath);
                zos.putNextEntry(entry);

                Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }
        }
    }

    private void extractZipArchive(File zipFile, File destDir) throws IOException {
        try (FileInputStream fis = new FileInputStream(zipFile);
             ZipInputStream zis = new ZipInputStream(fis)) {

            ZipEntry entry;
            while ((entry = zis. getNextEntry()) != null) {
                File entryFile = new File(destDir, entry.getName());

                if (entry.isDirectory()) {
                    entryFile.mkdirs();
                } else {
                    entryFile.getParentFile().mkdirs();
                    Files.copy(zis, entryFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                zis.closeEntry();
            }
        }
    }

    private String calculateSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
    }

    private void deleteDirectory(File directory) {
        if (directory. exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
}