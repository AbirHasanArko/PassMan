package com.passman.core.services;

import javax.crypto.SecretKey;

/**
 * Service for encrypting and decrypting passwords and data
 */
public interface EncryptionService {

    /**
     * Encrypts plaintext password
     */
    String encryptPassword(String plaintext, SecretKey masterKey) throws EncryptionException;

    /**
     * Decrypts password
     */
    String decryptPassword(String ciphertext, SecretKey masterKey) throws DecryptionException;

    /**
     * Encrypts file data
     */
    byte[] encryptFile(byte[] fileData, SecretKey masterKey) throws EncryptionException;

    /**
     * Decrypts file data
     */
    byte[] decryptFile(byte[] encryptedData, SecretKey masterKey) throws DecryptionException;

    /**
     * Derives master key from password
     */
    SecretKey deriveMasterKey(char[] password, byte[] salt) throws EncryptionException;

    /**
     * Generates new salt
     */
    byte[] generateSalt();
}