package com.passman.core.services;

/**
 * Exception thrown when decryption fails
 */
public class DecryptionException extends Exception {

    public DecryptionException(String message) {
        super(message);
    }

    public DecryptionException(String message, Throwable cause) {
        super(message, cause);
    }
}