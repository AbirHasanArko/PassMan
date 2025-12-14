package com.passman.core.crypto;

import org.gradle.internal.impldep.org.bouncycastle.crypto.SecureRandomProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * AES-256-CBC encryption/decryption implementation
 */
public class AESCipher {
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    private final SecureRandom secureRandom;

    public AESCipher() {
        this.secureRandom = SecureRandomProvider.getInstance();
    }

    /**
     * Encrypts plaintext with a unique IV
     * @param plaintext Data to encrypt
     * @param key Encryption key (256-bit)
     * @return Base64(IV + ciphertext)
     */
    public String encrypt(String plaintext, SecretKey key) throws Exception {
        try {
            // Generate unique IV for this encryption
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

            byte[] encrypted = cipher.doFinal(plaintext.getBytes("UTF-8"));

            // Prepend IV to ciphertext
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } finally {
            // Clear sensitive data
            if (plaintext != null) {
                char[] chars = plaintext.toCharArray();
                Arrays.fill(chars, '\0');
            }
        }
    }

    /**
     * Encrypts byte array (for file encryption)
     */
    public byte[] encryptBytes(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher. ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(data);

        // Prepend IV
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return combined;
    }

    /**
     * Decrypts ciphertext
     * @param ciphertext Base64(IV + encrypted data)
     * @param key Decryption key
     * @return Plaintext string
     */
    public String decrypt(String ciphertext, SecretKey key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(ciphertext);

        // Extract IV
        byte[] iv = new byte[IV_LENGTH];
        System. arraycopy(combined, 0, iv, 0, IV_LENGTH);

        // Extract ciphertext
        byte[] encrypted = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted. length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] decrypted = cipher. doFinal(encrypted);
        String result = new String(decrypted, "UTF-8");

        // Clear sensitive data
        Arrays.fill(decrypted, (byte) 0);
        Arrays.fill(iv, (byte) 0);

        return result;
    }

    /**
     * Decrypts byte array (for file decryption)
     */
    public byte[] decryptBytes(byte[] combined, SecretKey key) throws Exception {
        // Extract IV
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

        // Extract ciphertext
        byte[] encrypted = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher. DECRYPT_MODE, key, new IvParameterSpec(iv));

        return cipher.doFinal(encrypted);
    }
}