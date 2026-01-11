package com.passman.android.util;

/**
 * Application constants.
 */
public final class Constants {

    private Constants() {
        // Prevent instantiation
    }

    // Encryption
    public static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    public static final String KEY_ALGORITHM = "AES";
    public static final int KEY_SIZE = 256;
    public static final int IV_SIZE = 16;
    public static final int SALT_SIZE = 32;
    public static final int PBKDF2_ITERATIONS = 100000;

    // Session
    public static final long DEFAULT_SESSION_TIMEOUT = 5 * 60 * 1000; // 5 minutes
    public static final long MIN_SESSION_TIMEOUT = 60 * 1000; // 1 minute
    public static final long MAX_SESSION_TIMEOUT = 30 * 60 * 1000; // 30 minutes

    // Clipboard
    public static final long DEFAULT_CLIPBOARD_CLEAR_DELAY = 30 * 1000; // 30 seconds

    // Password Generation
    public static final int DEFAULT_PASSWORD_LENGTH = 16;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 64;
    
    public static final String UPPERCASE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String LOWERCASE_CHARS = "abcdefghijklmnopqrstuvwxyz";
    public static final String DIGIT_CHARS = "0123456789";
    public static final String SYMBOL_CHARS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    public static final String AMBIGUOUS_CHARS = "0O1lI";

    // Password Strength Thresholds
    public static final int STRENGTH_WEAK_MAX = 25;
    public static final int STRENGTH_FAIR_MAX = 50;
    public static final int STRENGTH_GOOD_MAX = 75;
    public static final int STRENGTH_STRONG_MAX = 90;

    // Password Age
    public static final int PASSWORD_OLD_DAYS = 90;
    public static final int PASSWORD_WARNING_DAYS = 60;

    // Categories
    public static final String CATEGORY_LOGIN = "Login";
    public static final String CATEGORY_CREDIT_CARD = "Credit Card";
    public static final String CATEGORY_IDENTITY = "Identity";
    public static final String CATEGORY_SECURE_NOTE = "Secure Note";
    public static final String CATEGORY_BANK = "Bank";
    public static final String CATEGORY_EMAIL = "Email";
    public static final String CATEGORY_SOCIAL = "Social Media";
    public static final String CATEGORY_WORK = "Work";
    public static final String CATEGORY_GAMING = "Gaming";
    public static final String CATEGORY_STREAMING = "Streaming";
    public static final String CATEGORY_SHOPPING = "Shopping";
    public static final String CATEGORY_OTHER = "Other";

    // Intent Extras
    public static final String EXTRA_CREDENTIAL_ID = "credential_id";
    public static final String EXTRA_IS_EDIT_MODE = "is_edit_mode";
    public static final String EXTRA_GENERATED_PASSWORD = "generated_password";

    // Shared Preferences
    public static final String PREF_BIOMETRIC_ENABLED = "biometric_enabled";
    public static final String PREF_AUTO_LOCK_TIMEOUT = "auto_lock_timeout";
    public static final String PREF_CLIPBOARD_TIMEOUT = "clipboard_timeout";
    public static final String PREF_DARK_MODE = "dark_mode";
    public static final String PREF_FIRST_LAUNCH = "first_launch";
    public static final String PREF_LAST_ACTIVE = "last_active";

    // Request Codes
    public static final int REQUEST_GENERATE_PASSWORD = 100;
    public static final int REQUEST_PICK_FILE = 101;
    public static final int REQUEST_CREATE_FILE = 102;

    // Database
    public static final String DATABASE_NAME = "passman_db";
    public static final int DATABASE_VERSION = 1;
}
