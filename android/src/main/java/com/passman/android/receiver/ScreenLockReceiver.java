package com.passman.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.passman.android.PassManApp;
import com.passman.android.security.SessionManager;

/**
 * Broadcast receiver for screen lock events to auto-lock the vault
 */
public class ScreenLockReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }

        String action = intent.getAction();
        
        if (Intent.ACTION_SCREEN_OFF.equals(action)) {
            // Screen turned off - check if we should lock immediately
            handleScreenOff(context);
        } else if (Intent.ACTION_USER_PRESENT.equals(action)) {
            // User unlocked the device
            handleUserPresent(context);
        }
    }

    private void handleScreenOff(Context context) {
        try {
            PassManApp app = (PassManApp) context.getApplicationContext();
            SessionManager sessionManager = app.getSessionManager();
            
            // Get auto-lock preference (0 means lock immediately)
            android.content.SharedPreferences prefs = 
                    context.getSharedPreferences("passman_settings", Context.MODE_PRIVATE);
            int autoLockMinutes = prefs.getInt("auto_lock_timeout", 5);
            
            if (autoLockMinutes == 0) {
                // Lock immediately when screen turns off
                sessionManager.lockVault();
            }
        } catch (Exception e) {
            // Ignore - app may not be initialized
        }
    }

    private void handleUserPresent(Context context) {
        try {
            PassManApp app = (PassManApp) context.getApplicationContext();
            SessionManager sessionManager = app.getSessionManager();
            
            // Get auto-lock preference
            android.content.SharedPreferences prefs = 
                    context.getSharedPreferences("passman_settings", Context.MODE_PRIVATE);
            int autoLockMinutes = prefs.getInt("auto_lock_timeout", 5);
            
            // Check if session has timed out
            if (autoLockMinutes > 0 && sessionManager.isSessionTimedOut(autoLockMinutes)) {
                sessionManager.lockVault();
            }
        } catch (Exception e) {
            // Ignore - app may not be initialized
        }
    }
}
