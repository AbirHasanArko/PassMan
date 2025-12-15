package com.passman.core.services;

import com.passman.core.crypto.AESCipher;
import com. passman.core.crypto.CipherFactory;
import com.passman.core.model.EncryptedFile;

import javax.crypto.SecretKey;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

/**
 * Service for encrypting and decrypting files
 */
public class FileEncryptionService {

    private final AESCipher aesCipher;
    private final Path encryptedFilesPath;

    public FileEncryptionService(String storagePath) {
        this.aesCipher = CipherFactory.createAESCipher();
        this.encryptedFilesPath = Paths.get(storagePath, "encrypted_files");
        initializeStorage();
    }

    private void initializeStorage() {
        try {
            Files.createDirectories(encryptedFilesPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize encrypted files storage", e);
        }
    }

    /**
     * Encrypt a file and return metadata
     */
    public EncryptedFile encryptFile(File sourceFile, Long vaultId, SecretKey key) throws Exception {
        // Read source file
        byte[] fileData = Files.readAllBytes(sourceFile.toPath());
        long originalSize = fileData.length;

        // Encrypt file data
        byte[] encryptedData = aesCipher.encryptBytes(fileData, key);

        // Extract IV from encrypted data
        byte[] iv = new byte[16];
        System.arraycopy(encryptedData, 0, iv, 0, 16);

        // Generate unique filename
        String encryptedFileName = UUID.randomUUID().toString() + ".enc";
        Path vaultPath = encryptedFilesPath.resolve(vaultId. toString());
        Files.createDirectories(vaultPath);

        Path encryptedFilePath = vaultPath.resolve(encryptedFileName);
        Files.write(encryptedFilePath, encryptedData);

        // Calculate checksum
        String checksum = calculateSHA256(fileData);

        // Create metadata
        EncryptedFile encryptedFile = new EncryptedFile();
        encryptedFile.setVaultId(vaultId);
        encryptedFile.setOriginalFileName(sourceFile.getName());
        encryptedFile.setEncryptedFileName(encryptedFileName);
        encryptedFile.setOriginalSize(originalSize);
        encryptedFile.setEncryptedSize(encryptedData.length);
        encryptedFile.setMimeType(Files.probeContentType(sourceFile.toPath()));
        encryptedFile.setEncryptionIV(iv);
        encryptedFile.setChecksum(checksum);
        encryptedFile.setUploadedAt(LocalDateTime.now());

        return encryptedFile;
    }

    /**
     * Decrypt a file and save to destination
     */
    public File decryptFile(EncryptedFile encryptedFile, File destinationFile, SecretKey key) throws Exception {
        // Read encrypted file
        Path encryptedFilePath = encryptedFilesPath.resolve(encryptedFile.getVaultId().toString())
                .resolve(encryptedFile. getEncryptedFileName());

        byte[] encryptedData = Files.readAllBytes(encryptedFilePath);

        // Decrypt
        byte[] decryptedData = aesCipher.decryptBytes(encryptedData, key);

        // Verify checksum
        String checksum = calculateSHA256(decryptedData);
        if (!checksum. equals(encryptedFile.getChecksum())) {
            throw new SecurityException("File integrity check failed");
        }

        // Write to destination
        Files.write(destinationFile.toPath(), decryptedData);

        return destinationFile;
    }

    /**
     * Delete encrypted file from storage
     */
    public void deleteEncryptedFile(EncryptedFile encryptedFile) throws IOException {
        Path filePath = encryptedFilesPath.resolve(encryptedFile.getVaultId().toString())
                .resolve(encryptedFile. getEncryptedFileName());
        Files.deleteIfExists(filePath);
    }

    /**
     * Get storage path for vault
     */
    public Path getVaultStoragePath(Long vaultId) {
        return encryptedFilesPath.resolve(vaultId.toString());
    }

    private String calculateSHA256(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest. getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return Base64.getEncoder().encodeToString(hash);
    }
}