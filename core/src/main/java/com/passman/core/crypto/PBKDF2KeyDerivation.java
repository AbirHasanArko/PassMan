package com.passman.core.crypto;

import org.gradle.internal.impldep.org.bouncycastle.crypto.SecureRandomProvider;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

/**
 * PBKDF2 key derivation with SHA-256
 */
public class PBKDF2KeyDerivation {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 100000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 32;

    /**
     * Derives encryption key from master password
     */
    public SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);

        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        SecretKey key = new SecretKeySpec(keyBytes, "AES");

        // Clear sensitive data
        Arrays.fill(keyBytes, (byte) 0);
        Arrays.fill(password, '\0');

        return key;
    }

    /**
     * Generates cryptographically secure salt
     */
    public byte[] generateSalt() {
        SecureRandom random = SecureRandomProvider.getInstance();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes password for storage (authentication)
     */
    public byte[] hashPassword(char[] password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);

        byte[] hash = factory.generateSecret(spec).getEncoded();
        Arrays.fill(password, '\0');

        return hash;
    }

    /**
     * Verifies password against stored hash
     */
    public boolean verifyPassword(char[] password, byte[] salt, byte[] expectedHash) throws Exception {
        byte[] actualHash = hashPassword(password, salt);
        boolean matches = Arrays.equals(actualHash, expectedHash);

        Arrays.fill(actualHash, (byte) 0);

        return matches;
    }
}