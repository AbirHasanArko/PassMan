package com.passman.core. services;

import com.passman.core.crypto.AESCipher;
import com.passman.core.crypto.CipherFactory;
import com.passman.core.crypto. PBKDF2KeyDerivation;

import javax.crypto.SecretKey;
import java.util.Arrays;

/**
 * Implementation of encryption service
 */
public class EncryptionServiceImpl implements EncryptionService {

    private final AESCipher aesCipher;
    private final PBKDF2KeyDerivation keyDerivation;

    public EncryptionServiceImpl() {
        this.aesCipher = CipherFactory.createAESCipher();
        this.keyDerivation = CipherFactory.createKeyDerivation();
    }

    @Override
    public String encryptPassword(String plaintext, SecretKey masterKey) throws EncryptionException {
        try {
            return aesCipher.encrypt(plaintext, masterKey);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt password", e);
        }
    }

    @Override
    public String decryptPassword(String ciphertext, SecretKey masterKey) throws DecryptionException {
        try {
            return aesCipher.decrypt(ciphertext, masterKey);
        } catch (Exception e) {
            throw new DecryptionException("Failed to decrypt password", e);
        }
    }

    @Override
    public byte[] encryptFile(byte[] fileData, SecretKey masterKey) throws EncryptionException {
        try {
            return aesCipher.encryptBytes(fileData, masterKey);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt file", e);
        }
    }

    @Override
    public byte[] decryptFile(byte[] encryptedData, SecretKey masterKey) throws DecryptionException {
        try {
            return aesCipher.decryptBytes(encryptedData, masterKey);
        } catch (Exception e) {
            throw new DecryptionException("Failed to decrypt file", e);
        }
    }

    @Override
    public SecretKey deriveMasterKey(char[] password, byte[] salt) throws EncryptionException {
        try {
            SecretKey key = keyDerivation.deriveKey(password, salt);
            Arrays.fill(password, '\0');
            return key;
        } catch (Exception e) {
            throw new EncryptionException("Failed to derive master key", e);
        }
    }

    @Override
    public byte[] generateSalt() {
        return keyDerivation.generateSalt();
    }
}