package com.passman.android.security;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Manages user session and encryption key storage
 */
public class SessionManager {

    private static final String PREFS_NAME = "passman_session";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_LAST_ACTIVE = "last_active";
    private static final String KEY_ENCRYPTED_KEY = "encrypted_master_key";

    private final SharedPreferences preferences;
    private final SharedPreferences securePreferences;
    private SecretKey masterKey;
    private long userId;
    private boolean isLoggedIn;

    public SessionManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        securePreferences = createSecurePreferences(context);
        loadSession();
    }

    private SharedPreferences createSecurePreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return EncryptedSharedPreferences.create(
                    context,
                    "passman_secure_prefs",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            // Fallback to regular preferences (less secure)
            return context.getSharedPreferences("passman_secure_fallback", Context.MODE_PRIVATE);
        }
    }

    private void loadSession() {
        isLoggedIn = preferences.getBoolean(KEY_IS_LOGGED_IN, false);
        userId = preferences.getLong(KEY_USER_ID, -1);
    }

    /**
     * Initialize a new session after successful authentication
     */
    public void initSession(long userId, SecretKey key) {
        this.userId = userId;
        this.masterKey = key;
        this.isLoggedIn = true;

        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .putLong(KEY_USER_ID, userId)
                .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
                .apply();
    }

    /**
     * Store the master key securely for biometric unlock
     */
    public void storeMasterKeyForBiometric(byte[] keyBytes) {
        String keyString = android.util.Base64.encodeToString(keyBytes, android.util.Base64.NO_WRAP);
        securePreferences.edit()
                .putString(KEY_ENCRYPTED_KEY, keyString)
                .apply();
    }

    /**
     * Retrieve the master key for biometric unlock
     */
    public SecretKey retrieveMasterKeyForBiometric() {
        String keyString = securePreferences.getString(KEY_ENCRYPTED_KEY, null);
        if (keyString != null) {
            byte[] keyBytes = android.util.Base64.decode(keyString, android.util.Base64.NO_WRAP);
            return new SecretKeySpec(keyBytes, "AES");
        }
        return null;
    }

    /**
     * Clear biometric key storage
     */
    public void clearBiometricKey() {
        securePreferences.edit()
                .remove(KEY_ENCRYPTED_KEY)
                .apply();
    }

    /**
     * Update last active timestamp
     */
    public void updateLastActive() {
        preferences.edit()
                .putLong(KEY_LAST_ACTIVE, System.currentTimeMillis())
                .apply();
    }

    /**
     * Check if session has timed out
     */
    public boolean isSessionTimedOut(int timeoutMinutes) {
        long lastActive = preferences.getLong(KEY_LAST_ACTIVE, 0);
        long elapsed = System.currentTimeMillis() - lastActive;
        return elapsed > (timeoutMinutes * 60 * 1000L);
    }

    /**
     * Clear the current session
     */
    public void clearSession() {
        // Securely clear the master key
        if (masterKey != null) {
            try {
                byte[] keyBytes = masterKey.getEncoded();
                if (keyBytes != null) {
                    Arrays.fill(keyBytes, (byte) 0);
                }
            } catch (Exception ignored) {}
            masterKey = null;
        }

        isLoggedIn = false;
        userId = -1;

        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .remove(KEY_USER_ID)
                .remove(KEY_LAST_ACTIVE)
                .apply();
    }

    /**
     * Lock the vault (keep user but clear key)
     */
    public void lockVault() {
        if (masterKey != null) {
            try {
                byte[] keyBytes = masterKey.getEncoded();
                if (keyBytes != null) {
                    Arrays.fill(keyBytes, (byte) 0);
                }
            } catch (Exception ignored) {}
            masterKey = null;
        }
        isLoggedIn = false;

        preferences.edit()
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    // ==================== GETTERS ====================

    public boolean isLoggedIn() {
        return isLoggedIn && masterKey != null;
    }

    public long getUserId() {
        return userId;
    }

    public SecretKey getMasterKey() {
        return masterKey;
    }

    public void setMasterKey(SecretKey key) {
        this.masterKey = key;
    }

    public boolean hasBiometricKey() {
        return securePreferences.contains(KEY_ENCRYPTED_KEY);
    }
}
