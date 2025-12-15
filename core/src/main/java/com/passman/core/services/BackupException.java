package com.passman.core.services;

/**
 * Exception thrown when backup operations fail
 */
public class BackupException extends Exception {

    public BackupException(String message) {
        super(message);
    }

    public BackupException(String message, Throwable cause) {
        super(message, cause);
    }
}