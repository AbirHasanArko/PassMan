package com.passman.core.services;

import com.passman.core.crypto.PBKDF2KeyDerivation;
import com.passman.core.db.DatabaseManager;
import com.passman.core.model.FileVault;
import com.passman.core.repository.FileVaultRepository;

import javax.crypto.SecretKey;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing file vaults with individual passwords
 */
public class FileVaultService {

    private final FileVaultRepository vaultRepository;
    private final PBKDF2KeyDerivation keyDerivation;

    public FileVaultService(FileVaultRepository vaultRepository) {
        this.vaultRepository = vaultRepository;
        this.keyDerivation = new PBKDF2KeyDerivation();
    }

    /**
     * Create a new vault with optional separate password
     */
    public FileVault createVault(String vaultName, FileVault.VaultType vaultType,
                                 String iconEmoji, char[] separatePassword) throws Exception {
        FileVault vault = new FileVault();
        vault.setVaultName(vaultName);
        vault.setVaultType(vaultType);
        vault.setIconEmoji(iconEmoji);
        vault.setHasSeparatePassword(separatePassword != null);

        if (separatePassword != null) {
            byte[] salt = keyDerivation.generateSalt();
            byte[] passwordHash = keyDerivation.hashPassword(separatePassword, salt);

            vault.setVaultSalt(salt);
            vault. setVaultPasswordHash(passwordHash);

            Arrays.fill(separatePassword, '\0');
            Arrays.fill(passwordHash, (byte) 0);
        }

        return vaultRepository.save(vault);
    }

    /**
     * Unlock a vault and get its encryption key
     */
    public SecretKey unlockVault(Long vaultId, char[] password, SecretKey masterKey) throws Exception {
        Optional<FileVault> vaultOpt = vaultRepository.findById(vaultId);
        if (vaultOpt.isEmpty()) {
            throw new IllegalArgumentException("Vault not found");
        }

        FileVault vault = vaultOpt.get();

        // If vault has no separate password, use master key
        if (!vault.isHasSeparatePassword()) {
            return masterKey;
        }

        // Verify vault password
        if (password == null) {
            throw new IllegalArgumentException("Vault password required");
        }

        boolean valid = keyDerivation.verifyPassword(
                password,
                vault.getVaultSalt(),
                vault.getVaultPasswordHash()
        );

        if (!valid) {
            throw new SecurityException("Invalid vault password");
        }

        // Derive key from vault password
        SecretKey vaultKey = keyDerivation.deriveKey(password, vault.getVaultSalt());
        Arrays.fill(password, '\0');

        return vaultKey;
    }

    /**
     * Set or change vault password
     */
    public void setVaultPassword(Long vaultId, char[] newPassword) throws Exception {
        Optional<FileVault> vaultOpt = vaultRepository.findById(vaultId);
        if (vaultOpt.isEmpty()) throw new IllegalArgumentException("Vault not found");

        FileVault vault = vaultOpt.get();

        if (newPassword != null && newPassword.length > 0) {
            byte[] salt = vault.getVaultSalt();
            if (salt == null) salt = keyDerivation.generateSalt(); // reuse existing salt if present

            byte[] passwordHash = keyDerivation.hashPassword(newPassword, salt);

            vault.setVaultSalt(salt);
            vault.setVaultPasswordHash(passwordHash);
            vault.setHasSeparatePassword(true);

            vaultRepository.update(vault); // make sure DB is updated here

            Arrays.fill(newPassword, '\0');
            Arrays.fill(passwordHash, (byte) 0);
        } else {
            // Remove password
            vault.setVaultSalt(null);
            vault.setVaultPasswordHash(null);
            vault.setHasSeparatePassword(false);

            vaultRepository.update(vault);
        }
    }

    /**
     * Remove vault password (revert to master password)
     */
    public void removeVaultPassword(Long vaultId, char[] currentPassword) throws Exception {
        unlockVault(vaultId, currentPassword, null);
        setVaultPassword(vaultId, null);
    }

    /**
     * Check if vault has separate password
     */
    public boolean hasSeparatePassword(Long vaultId) throws Exception {
        Optional<FileVault> vaultOpt = vaultRepository.findById(vaultId);
        return vaultOpt.map(FileVault::isHasSeparatePassword).orElse(false);
    }

    /**
     * Get all vaults
     */
    public List<FileVault> getAllVaults() throws Exception {
        return vaultRepository.findAll();
    }

    /**
     * Get vault by type
     */
    public Optional<FileVault> getVaultByType(FileVault.VaultType type) throws Exception {
        return vaultRepository.findByType(type);
    }
}