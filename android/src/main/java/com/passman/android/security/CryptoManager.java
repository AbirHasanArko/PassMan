package com.passman.android.security;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Encryption utilities for password storage
 */
public class CryptoManager {

    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int IV_LENGTH = 16;
    private static final int SALT_LENGTH = 32;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATIONS = 100000;

    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generate a random salt for password hashing
     */
    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        secureRandom.nextBytes(salt);
        return salt;
    }

    /**
     * Derive a SecretKey from password and salt using PBKDF2
     */
    public static SecretKey deriveKey(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Hash a password with salt for storage
     */
    public static byte[] hashPassword(char[] password, byte[] salt) throws Exception {
        PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
            return factory.generateSecret(spec).getEncoded();
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Verify a password against stored hash
     */
    public static boolean verifyPassword(char[] password, byte[] salt, byte[] storedHash) 
            throws Exception {
        byte[] computedHash = hashPassword(password, salt);
        boolean result = slowEquals(computedHash, storedHash);
        Arrays.fill(computedHash, (byte) 0);
        return result;
    }

    /**
     * Constant-time comparison to prevent timing attacks
     */
    private static boolean slowEquals(byte[] a, byte[] b) {
        if (a.length != b.length) return false;
        int diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff |= a[i] ^ b[i];
        }
        return diff == 0;
    }

    /**
     * Encrypt a string with the given key
     */
    public static String encrypt(String plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Combine IV and encrypted data
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * Decrypt a string with the given key
     */
    public static String decrypt(String ciphertext, SecretKey key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(ciphertext);

        // Extract IV
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

        // Extract encrypted data
        byte[] encrypted = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] decrypted = cipher.doFinal(encrypted);
        String result = new String(decrypted, StandardCharsets.UTF_8);

        // Clear sensitive data
        Arrays.fill(decrypted, (byte) 0);
        Arrays.fill(iv, (byte) 0);

        return result;
    }

    /**
     * Encrypt bytes with the given key
     */
    public static byte[] encryptBytes(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(data);

        // Combine IV and encrypted data
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return combined;
    }

    /**
     * Decrypt bytes with the given key
     */
    public static byte[] decryptBytes(byte[] combined, SecretKey key) throws Exception {
        // Extract IV
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH);

        // Extract encrypted data
        byte[] encrypted = new byte[combined.length - IV_LENGTH];
        System.arraycopy(combined, IV_LENGTH, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));

        return cipher.doFinal(encrypted);
    }

    /**
     * Generate a secure random password
     */
    public static String generatePassword(int length, boolean uppercase, boolean lowercase,
                                          boolean numbers, boolean symbols, 
                                          boolean excludeAmbiguous) {
        StringBuilder chars = new StringBuilder();
        
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String nums = "0123456789";
        String syms = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String ambiguous = "0O1lI";

        if (uppercase) chars.append(upper);
        if (lowercase) chars.append(lower);
        if (numbers) chars.append(nums);
        if (symbols) chars.append(syms);

        if (chars.length() == 0) {
            chars.append(lower).append(nums);
        }

        String charSet = chars.toString();
        if (excludeAmbiguous) {
            for (char c : ambiguous.toCharArray()) {
                charSet = charSet.replace(String.valueOf(c), "");
            }
        }

        StringBuilder password = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(charSet.length());
            password.append(charSet.charAt(index));
        }

        return password.toString();
    }

    private static final PasswordStrengthService strengthService = new PasswordStrengthService();

    /**
     * Calculate password strength score (0-100) with personal info detection
     */
    public static int calculatePasswordStrength(String password) {
        return strengthService.getScore(password);
    }

    /**
     * Calculate password strength with personal info context
     */
    public static int calculatePasswordStrength(String password, 
                                                  PasswordStrengthService.PersonalInfoContext context) {
        return strengthService.getScore(password, context);
    }

    /**
     * Get full strength result with warnings
     */
    public static PasswordStrengthService.StrengthResult getPasswordStrengthResult(String password) {
        return strengthService.calculateStrength(password);
    }

    /**
     * Get full strength result with warnings and personal info context
     */
    public static PasswordStrengthService.StrengthResult getPasswordStrengthResult(
            String password, PasswordStrengthService.PersonalInfoContext context) {
        return strengthService.calculateStrength(password, context);
    }

    /**
     * Get strength label for score
     */
    public static String getStrengthLabel(int score) {
        if (score < 20) return "Weak";
        if (score < 40) return "Fair";
        if (score < 60) return "Good";
        if (score < 80) return "Strong";
        return "Very Strong";
    }
}
