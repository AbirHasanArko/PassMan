package com.passman.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.passman.android.data.database.PassManDatabase;
import com.passman.android.data.repository.CredentialRepository;
import com.passman.android.data.repository.UserRepository;
import com.passman.android.security.SessionManager;
import com.passman.android.security.BiometricManager;
import com.passman.android.notification.NotificationHelper;
import com.passman.android.notification.NotificationScheduler;
import com.passman.android.worker.CardExpirationWorker;

import java.util.concurrent.TimeUnit;

/**
 * Main Application class for PassMan Android
 */
public class PassManApp extends Application {

    private static final String PREFS_NAME = "passman_settings";
    private static final String KEY_DARK_MODE = "dark_mode";

    private static PassManApp instance;
    private PassManDatabase database;
    private CredentialRepository credentialRepository;
    private UserRepository userRepository;
    private SessionManager sessionManager;
    private BiometricManager biometricManager;
    private NotificationHelper notificationHelper;
    private NotificationScheduler notificationScheduler;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        
        // Apply saved theme setting before anything else
        applyThemeSetting();
        
        // Initialize database
        database = PassManDatabase.getInstance(this);
        
        // Initialize repositories
        credentialRepository = new CredentialRepository(database.credentialDao());
        userRepository = new UserRepository(database.userDao());
        
        // Initialize managers
        sessionManager = new SessionManager(this);
        biometricManager = new BiometricManager(this);
        
        // Initialize notification system
        notificationHelper = new NotificationHelper(this);
        notificationScheduler = new NotificationScheduler(this);
        
        // Schedule security checks if notifications are enabled
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
        if (notificationsEnabled) {
            notificationScheduler.scheduleSecurityChecks();
            scheduleCardExpirationChecks();
        }
    }

    /**
     * Schedule periodic card expiration checks (daily)
     */
    private void scheduleCardExpirationChecks() {
        PeriodicWorkRequest cardCheckRequest = new PeriodicWorkRequest.Builder(
                CardExpirationWorker.class,
                24, TimeUnit.HOURS)  // Run once per day
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                CardExpirationWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                cardCheckRequest);
    }

    private void applyThemeSetting() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        int darkMode = prefs.getInt(KEY_DARK_MODE, 0);
        
        switch (darkMode) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case 2:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public static PassManApp getInstance() {
        return instance;
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    public PassManDatabase getDatabase() {
        return database;
    }

    public CredentialRepository getCredentialRepository() {
        return credentialRepository;
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public BiometricManager getBiometricManager() {
        return biometricManager;
    }

    public NotificationHelper getNotificationHelper() {
        return notificationHelper;
    }

    public NotificationScheduler getNotificationScheduler() {
        return notificationScheduler;
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        // Clear sensitive data on app termination
        if (sessionManager != null) {
            sessionManager.clearSession();
        }
    }
}
