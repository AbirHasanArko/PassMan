package com.passman.core.services;

import javax.crypto.SecretKey;

/**
 * Service interface for encryption/decryption operations
 */
public interface EncryptionService {

    /**
     * Encrypt a password string
     */
    String encryptPassword(String plaintext, SecretKey key) throws EncryptionException;

    /**
     * Decrypt a password string
     */
    String decryptPassword(String ciphertext, SecretKey key) throws DecryptionException;

    /**
     * Encrypt byte array (for files)
     */
    byte[] encryptBytes(byte[] data, SecretKey key) throws EncryptionException;

    /**
     * Decrypt byte array (for files)
     */
    byte[] decryptBytes(byte[] data, SecretKey key) throws DecryptionException;
}