package com.passman.core.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Singleton provider for cryptographically secure random number generator
 */
public class SecureRandomProvider {
    private static volatile SecureRandom instance;

    private SecureRandomProvider() {}

    public static SecureRandom getInstance() {
        if (instance == null) {
            synchronized (SecureRandomProvider.class) {
                if (instance == null) {
                    try {
                        instance = SecureRandom.getInstanceStrong();
                    } catch (NoSuchAlgorithmException e) {
                        instance = new SecureRandom();
                    }
                }
            }
        }
        return instance;
    }
}