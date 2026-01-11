package com.passman.android;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import com.passman.core.model.User;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

/**
 * SessionManager handles user session state and master key management for the Android application.
 */
public class SessionManager {
    private static volatile SessionManager instance;
    private final SharedPreferences sharedPreferences;
    private final Context context;
    private SecretKey masterKey;
    private User currentUser;

    private SessionManager(Context context) {
        this.context = context.getApplicationContext();
        try {
            MasterKey masterKeySpec = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            this.sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    "session_prefs",
                    masterKeySpec,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize SessionManager", e);
        }
    }

    public static SessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager(context);
                }
            }
        }
        return instance;
    }

    public static SessionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SessionManager not initialized. Call getInstance(Context) first");
        }
        return instance;
    }

    /**
     * Sets the master key for the session
     */
    public void setMasterKey(SecretKey key) {
        this.masterKey = key;
        // Store key material in encrypted shared preferences (base64 encoded)
        if (key != null) {
            String encodedKey = java.util.Base64.getEncoder().encodeToString(key.getEncoded());
            sharedPreferences.edit().putString("master_key", encodedKey).apply();
        }
    }

    /**
     * Gets the master key for the session
     */
    public SecretKey getMasterKey() {
        if (masterKey != null) {
            return masterKey;
        }

        // Try to retrieve from shared preferences
        String encodedKey = sharedPreferences.getString("master_key", null);
        if (encodedKey != null) {
            byte[] decodedKey = java.util.Base64.getDecoder().decode(encodedKey);
            masterKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
            return masterKey;
        }

        return null;
    }

    /**
     * Sets the current logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gets the current logged-in user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is logged in
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Clears the session
     */
    public void clearSession() {
        this.masterKey = null;
        this.currentUser = null;
        sharedPreferences.edit().clear().apply();
    }

    /**
     * Gets a string value from session storage
     */
    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    /**
     * Sets a string value in session storage
     */
    public void setString(String key, String value) {
        sharedPreferences.edit().putString(key, value).apply();
    }

    /**
     * Gets a boolean value from session storage
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return sharedPreferences.getBoolean(key, defaultValue);
    }

    /**
     * Sets a boolean value in session storage
     */
    public void setBoolean(String key, boolean value) {
        sharedPreferences.edit().putBoolean(key, value).apply();
    }
}
