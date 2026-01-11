package com.passman.android.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.passman.android.notification.NotificationScheduler;

/**
 * Receiver that triggers when the device boots up.
 * Used to restart the notification scheduler after device restart.
 */
public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Check if notifications are enabled
            SharedPreferences prefs = context.getSharedPreferences("passman_settings", Context.MODE_PRIVATE);
            boolean notificationsEnabled = prefs.getBoolean("notifications_enabled", true);
            
            if (notificationsEnabled) {
                // Restart the notification scheduler
                NotificationScheduler scheduler = new NotificationScheduler(context);
                scheduler.scheduleSecurityChecks();
            }
        }
    }
}
