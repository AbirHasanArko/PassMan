package com.passman.core.services;

import com.passman.core.crypto.AESCipher;
import com. passman.core.crypto.CipherFactory;

import javax.crypto.SecretKey;

/**
 * Implementation of encryption service using AES-256-CBC
 */
public class EncryptionServiceImpl implements EncryptionService {

    private final AESCipher aesCipher;

    public EncryptionServiceImpl() {
        this.aesCipher = CipherFactory. createAESCipher();
    }

    @Override
    public String encryptPassword(String plaintext, SecretKey key) throws EncryptionException {
        try {
            return aesCipher.encrypt(plaintext, key);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt password", e);
        }
    }

    @Override
    public String decryptPassword(String ciphertext, SecretKey key) throws DecryptionException {
        try {
            return aesCipher.decrypt(ciphertext, key);
        } catch (Exception e) {
            throw new DecryptionException("Failed to decrypt password", e);
        }
    }

    @Override
    public byte[] encryptBytes(byte[] data, SecretKey key) throws EncryptionException {
        try {
            return aesCipher. encryptBytes(data, key);
        } catch (Exception e) {
            throw new EncryptionException("Failed to encrypt bytes", e);
        }
    }

    @Override
    public byte[] decryptBytes(byte[] data, SecretKey key) throws DecryptionException {
        try {
            return aesCipher.decryptBytes(data, key);
        } catch (Exception e) {
            throw new DecryptionException("Failed to decrypt bytes", e);
        }
    }
}