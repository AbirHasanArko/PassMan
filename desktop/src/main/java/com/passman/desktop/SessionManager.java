package com.passman.desktop;

import com.passman. core.model.User;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util. Arrays;

/**
 * Singleton session manager for user authentication state
 */
public class SessionManager {

    private static volatile SessionManager instance;

    private User currentUser;
    private SecretKey masterKey;
    private LocalDateTime loginTime;
    private LocalDateTime lastActivityTime;

    // Session timeout in minutes
    private static final int SESSION_TIMEOUT_MINUTES = 30;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            synchronized (SessionManager.class) {
                if (instance == null) {
                    instance = new SessionManager();
                }
            }
        }
        return instance;
    }

    /**
     * Initialize session with user and master key
     */
    public void initSession(User user, SecretKey masterKey) {
        this.currentUser = user;
        this. masterKey = masterKey;
        this.loginTime = LocalDateTime. now();
        this.lastActivityTime = LocalDateTime.now();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        if (currentUser == null || masterKey == null) {
            return false;
        }

        // Check session timeout
        if (isSessionExpired()) {
            clearSession();
            return false;
        }

        return true;
    }

    /**
     * Check if session has expired
     */
    public boolean isSessionExpired() {
        if (lastActivityTime == null) {
            return true;
        }

        LocalDateTime expiryTime = lastActivityTime. plusMinutes(SESSION_TIMEOUT_MINUTES);
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Update last activity time
     */
    public void updateActivity() {
        this.lastActivityTime = LocalDateTime. now();
    }

    /**
     * Get current user
     */
    public User getCurrentUser() {
        updateActivity();
        return currentUser;
    }

    /**
     * Set current user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
        updateActivity();
    }

    /**
     * Get master encryption key
     */
    public SecretKey getMasterKey() {
        updateActivity();
        return masterKey;
    }

    /**
     * Set master encryption key
     */
    public void setMasterKey(SecretKey masterKey) {
        this.masterKey = masterKey;
        updateActivity();
    }

    /**
     * Get login time
     */
    public LocalDateTime getLoginTime() {
        return loginTime;
    }

    /**
     * Get time until session expires (in minutes)
     */
    public long getMinutesUntilExpiry() {
        if (lastActivityTime == null) {
            return 0;
        }

        LocalDateTime expiryTime = lastActivityTime.plusMinutes(SESSION_TIMEOUT_MINUTES);
        long minutes = java.time.Duration.between(LocalDateTime.now(), expiryTime).toMinutes();
        return Math.max(0, minutes);
    }

    /**
     * Clear session (logout)
     */
    public void clearSession() {
        this.currentUser = null;

        // Securely clear master key from memory
        if (this.masterKey != null) {
            try {
                byte[] encoded = this.masterKey.getEncoded();
                Arrays.fill(encoded, (byte) 0);
            } catch (Exception e) {
                // Ignore
            }
            this.masterKey = null;
        }

        this.loginTime = null;
        this.lastActivityTime = null;

        System.out.println("✅ Session cleared");
    }
}