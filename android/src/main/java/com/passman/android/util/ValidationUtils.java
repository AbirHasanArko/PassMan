package com.passman.android.util;

import android.content.Context;
import android.util.Patterns;

import com.passman.android.R;

/**
 * Utility class for input validation.
 */
public class ValidationUtils {

    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MIN_MASTER_PASSWORD_LENGTH = 12;

    /**
     * Validate email format.
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true; // Empty is valid (optional field)
        }
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    /**
     * Validate URL format.
     */
    public static boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return true; // Empty is valid (optional field)
        }
        return Patterns.WEB_URL.matcher(url).matches();
    }

    /**
     * Validate title (required field).
     */
    public static ValidationResult validateTitle(String title) {
        if (title == null || title.trim().isEmpty()) {
            return new ValidationResult(false, "Title is required");
        }
        if (title.length() > 100) {
            return new ValidationResult(false, "Title must be less than 100 characters");
        }
        return new ValidationResult(true, null);
    }

    /**
     * Validate password for credential.
     */
    public static ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Password is required");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return new ValidationResult(false, 
                "Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
        }
        return new ValidationResult(true, null);
    }

    /**
     * Validate master password.
     */
    public static ValidationResult validateMasterPassword(String password) {
        if (password == null || password.isEmpty()) {
            return new ValidationResult(false, "Master password is required");
        }
        if (password.length() < MIN_MASTER_PASSWORD_LENGTH) {
            return new ValidationResult(false, 
                "Master password must be at least " + MIN_MASTER_PASSWORD_LENGTH + " characters");
        }
        // Check for at least one uppercase
        if (!password.matches(".*[A-Z].*")) {
            return new ValidationResult(false, 
                "Password must contain at least one uppercase letter");
        }
        // Check for at least one lowercase
        if (!password.matches(".*[a-z].*")) {
            return new ValidationResult(false, 
                "Password must contain at least one lowercase letter");
        }
        // Check for at least one digit
        if (!password.matches(".*\\d.*")) {
            return new ValidationResult(false, 
                "Password must contain at least one number");
        }
        // Check for at least one special character
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return new ValidationResult(false, 
                "Password must contain at least one special character");
        }
        return new ValidationResult(true, null);
    }

    /**
     * Validate password confirmation.
     */
    public static ValidationResult validatePasswordConfirmation(String password, 
                                                                  String confirmation) {
        if (confirmation == null || confirmation.isEmpty()) {
            return new ValidationResult(false, "Please confirm your password");
        }
        if (!password.equals(confirmation)) {
            return new ValidationResult(false, "Passwords don't match");
        }
        return new ValidationResult(true, null);
    }

    /**
     * Result class for validation.
     */
    public static class ValidationResult {
        public final boolean isValid;
        public final String errorMessage;

        public ValidationResult(boolean isValid, String errorMessage) {
            this.isValid = isValid;
            this.errorMessage = errorMessage;
        }
    }
}
