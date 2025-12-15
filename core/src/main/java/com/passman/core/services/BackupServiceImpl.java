package com.passman.core.services;

import com.google.gson.Gson;
import com.passman.core.crypto.AESCipher;
import com.passman.core.crypto.CipherFactory;
import com.passman.core.db.DatabaseManager;
import com.passman.core.model.Backup;
import com.passman.core.repository.BackupRepository;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file. Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java. time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Implementation of backup service with encryption
 */
public class BackupServiceImpl implements BackupService {

    private final DatabaseManager dbManager;
    private final BackupRepository backupRepository;
    private final AESCipher aesCipher;
    private final Path backupStoragePath;
    private final Gson gson;

    public BackupServiceImpl(DatabaseManager dbManager, BackupRepository backupRepository, String storagePath) {
        this.dbManager = dbManager;
        this.backupRepository = backupRepository;
        this. aesCipher = CipherFactory.createAESCipher();
        this.backupStoragePath = Paths.get(storagePath, "backups");
        this.gson = new Gson();
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(backupStoragePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize backup storage", e);
        }
    }

    @Override
    public Backup createBackup(SecretKey masterKey, String description) throws BackupException {
        try {
            // Generate backup filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String backupFileName = "passman_backup_" + timestamp + ".pmbak";
            Path backupFilePath = backupStoragePath. resolve(backupFileName);

            // Get database file
            File dbFile = new File(dbManager.getDatabasePath());

            // Create encrypted zip backup
            try (FileOutputStream fos = new FileOutputStream(backupFilePath.toFile());
                 ZipOutputStream zos = new ZipOutputStream(fos)) {

                // Add database file
                ZipEntry dbEntry = new ZipEntry("passman. db");
                zos.putNextEntry(dbEntry);

                byte[] dbData = Files.readAllBytes(dbFile. toPath());
                byte[] encryptedData = aesCipher.encryptBytes(dbData, masterKey);
                zos.write(encryptedData);
                zos.closeEntry();

                // Add metadata
                BackupMetadata metadata = new BackupMetadata();
                metadata.timestamp = LocalDateTime.now();
                metadata.version = "1.0.0";
                metadata.description = description;

                ZipEntry metaEntry = new ZipEntry("metadata.json");
                zos. putNextEntry(metaEntry);
                String metaJson = gson.toJson(metadata);
                byte[] encryptedMeta = aesCipher. encryptBytes(metaJson. getBytes(), masterKey);
                zos.write(encryptedMeta);
                zos. closeEntry();
            }

            // Calculate checksum
            byte[] backupData = Files.readAllBytes(backupFilePath);
            String checksum = calculateSHA256(backupData);

            // Create backup metadata
            Backup backup = new Backup();
            backup.setBackupFileName(backupFileName);
            backup.setBackupPath(backupFilePath.toString());
            backup.setFileSize(backupData.length);
            backup.setChecksum(checksum);
            backup.setBackupType(Backup.BackupType. MANUAL);
            backup.setStatus(Backup.BackupStatus.COMPLETED);
            backup.setDescription(description);
            backup.setCreatedAt(LocalDateTime.now());

            return backupRepository.save(backup);

        } catch (Exception e) {
            throw new BackupException("Failed to create backup", e);
        }
    }

    @Override
    public void restoreBackup(File backupFile, SecretKey masterKey) throws BackupException {
        try {
            // Verify backup integrity
            if (!verifyBackup(backupFile)) {
                throw new BackupException("Backup file is corrupted");
            }

            // Extract and decrypt database
            try (FileInputStream fis = new FileInputStream(backupFile);
                 ZipInputStream zis = new ZipInputStream(fis)) {

                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if ("passman.db".equals(entry.getName())) {
                        byte[] encryptedData = zis.readAllBytes();
                        byte[] decryptedData = aesCipher.decryptBytes(encryptedData, masterKey);

                        // Close current database connection
                        dbManager.close();

                        // Replace database file
                        File dbFile = new File(dbManager.getDatabasePath());
                        Files.write(dbFile.toPath(), decryptedData);

                        // Reinitialize database
                        dbManager.initialize();

                        break;
                    }
                }
            }

        } catch (Exception e) {
            throw new BackupException("Failed to restore backup", e);
        }
    }

    @Override
    public boolean verifyBackup(File backupFile) throws BackupException {
        try {
            // Check if backup exists in database
            Optional<Backup> backupOpt = backupRepository.findByFileName(backupFile.getName());
            if (backupOpt.isEmpty()) {
                return false;
            }

            // Verify checksum
            byte[] fileData = Files.readAllBytes(backupFile.toPath());
            String calculatedChecksum = calculateSHA256(fileData);

            return calculatedChecksum.equals(backupOpt.get().getChecksum());

        } catch (Exception e) {
            throw new BackupException("Failed to verify backup", e);
        }
    }

    @Override
    public List<Backup> getAllBackups() throws BackupException {
        try {
            return backupRepository.findAll();
        } catch (Exception e) {
            throw new BackupException("Failed to fetch backups", e);
        }
    }

    @Override
    public void deleteBackup(Long backupId) throws BackupException {
        try {
            Optional<Backup> backupOpt = backupRepository.findById(backupId);
            if (backupOpt.isPresent()) {
                Backup backup = backupOpt. get();

                // Delete file
                File backupFile = new File(backup. getBackupPath());
                Files.deleteIfExists(backupFile.toPath());

                // Delete metadata
                backupRepository.delete(backupId);
            }
        } catch (Exception e) {
            throw new BackupException("Failed to delete backup", e);
        }
    }

    @Override
    public BackupStatistics getStatistics() throws BackupException {
        try {
            List<Backup> allBackups = backupRepository.findAll();

            BackupStatistics stats = new BackupStatistics();
            stats.totalBackups = allBackups.size();
            stats.totalSize = allBackups.stream().mapToLong(Backup:: getFileSize).sum();

            if (! allBackups.isEmpty()) {
                stats.latestBackup = allBackups.get(0);
                stats.oldestBackup = allBackups.get(allBackups.size() - 1);
            }

            return stats;

        } catch (Exception e) {
            throw new BackupException("Failed to get statistics", e);
        }
    }

    private String calculateSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
    }

    private static class BackupMetadata {
        LocalDateTime timestamp;
        String version;
        String description;
    }
}