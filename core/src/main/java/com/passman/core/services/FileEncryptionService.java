package com.passman.core.services;

import com.passman.core.crypto.AESCipher;
import com.passman.core.crypto. CipherFactory;
import com.passman.core.models.EncryptedFile;
import com.passman.core.models.FileVault;

import javax.crypto.SecretKey;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java. util.Base64;
import java.util.UUID;

/**
 * Service for encrypting and decrypting files
 */
public class FileEncryptionService {

    private final AESCipher aesCipher;
    private final Path vaultStoragePath;

    public FileEncryptionService(String storagePath) {
        this.aesCipher = CipherFactory.createAESCipher();
        this.vaultStoragePath = Paths.get(storagePath);
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(vaultStoragePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize vault storage", e);
        }
    }

    /**
     * Encrypts a file and stores it in the vault
     */
    public EncryptedFile encryptFile(File sourceFile, FileVault vault, SecretKey masterKey)
            throws EncryptionException {
        try {
            // Read file data
            byte[] fileData = Files.readAllBytes(sourceFile. toPath());

            // Calculate checksum
            String checksum = calculateSHA256(fileData);

            // Encrypt file data
            byte[] encryptedData = aesCipher.encryptBytes(fileData, masterKey);

            // Generate unique encrypted filename
            String encryptedFileName = UUID.randomUUID().toString() + ". enc";
            Path encryptedFilePath = vaultStoragePath. resolve(vault.getId().toString())
                    .resolve(encryptedFileName);

            // Ensure vault directory exists
            Files.createDirectories(encryptedFilePath.getParent());

            // Write encrypted data
            Files.write(encryptedFilePath, encryptedData);

            // Create metadata
            EncryptedFile encryptedFile = new EncryptedFile();
            encryptedFile.setVaultId(vault.getId());
            encryptedFile.setOriginalFileName(sourceFile.getName());
            encryptedFile.setEncryptedFileName(encryptedFileName);
            encryptedFile.setOriginalSize(fileData.length);
            encryptedFile.setEncryptedSize(encryptedData.length);
            encryptedFile.setMimeType(Files.probeContentType(sourceFile.toPath()));
            encryptedFile.setChecksum(checksum);
            encryptedFile.setUploadedAt(LocalDateTime.now());

            // Clear sensitive data
            java.util.Arrays.fill(fileData, (byte) 0);

            return encryptedFile;

        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt file: " + sourceFile.getName(), e);
        }
    }

    /**
     * Decrypts a file from the vault
     */
    public File decryptFile(EncryptedFile encryptedFile, File destinationFile, SecretKey masterKey)
            throws DecryptionException {
        try {
            // Read encrypted data
            Path encryptedFilePath = vaultStoragePath.resolve(encryptedFile.getVaultId().toString())
                    . resolve(encryptedFile.getEncryptedFileName());

            byte[] encryptedData = Files. readAllBytes(encryptedFilePath);

            // Decrypt file data
            byte[] decryptedData = aesCipher.decryptBytes(encryptedData, masterKey);

            // Verify checksum
            String checksum = calculateSHA256(decryptedData);
            if (!checksum.equals(encryptedFile.getChecksum())) {
                throw new DecryptionException("File integrity check failed");
            }

            // Write decrypted data
            Files. write(destinationFile.toPath(), decryptedData);

            // Update last accessed time
            encryptedFile.setLastAccessed(LocalDateTime.now());

            // Clear sensitive data
            java.util.Arrays.fill(decryptedData, (byte) 0);

            return destinationFile;

        } catch (Exception e) {
            throw new DecryptionException("Failed to decrypt file: " + encryptedFile.getOriginalFileName(), e);
        }
    }

    /**
     * Deletes encrypted file from storage
     */
    public void deleteEncryptedFile(EncryptedFile encryptedFile) throws IOException {
        Path encryptedFilePath = vaultStoragePath. resolve(encryptedFile.getVaultId().toString())
                .resolve(encryptedFile. getEncryptedFileName());
        Files.deleteIfExists(encryptedFilePath);
    }

    /**
     * Calculates SHA-256 checksum
     */
    private String calculateSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Gets total vault size in bytes
     */
    public long getVaultSize(Long vaultId) throws IOException {
        Path vaultPath = vaultStoragePath.resolve(vaultId.toString());
        if (!Files.exists(vaultPath)) {
            return 0;
        }

        return Files.walk(vaultPath)
                .filter(Files::isRegularFile)
                .mapToLong(p -> {
                    try {
                        return Files.size(p);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
    }
}