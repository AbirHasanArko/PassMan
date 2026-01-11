package com.passman.android.notification;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/**
 * Schedules periodic notification checks
 */
public class NotificationScheduler {

    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_SECURITY_ALERTS = "security_alerts_enabled";
    private static final String KEY_PASSWORD_REMINDERS = "password_reminders_enabled";
    private static final String KEY_CHECK_INTERVAL_HOURS = "check_interval_hours";

    private final Context context;
    private final SharedPreferences prefs;

    public NotificationScheduler(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Schedule periodic security checks
     */
    public void scheduleSecurityChecks() {
        if (!isSecurityAlertsEnabled() && !isPasswordRemindersEnabled()) {
            cancelSecurityChecks();
            return;
        }

        int intervalHours = getCheckIntervalHours();

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(true)
                .build();

        PeriodicWorkRequest securityCheckRequest = new PeriodicWorkRequest.Builder(
                SecurityCheckWorker.class,
                intervalHours, TimeUnit.HOURS,
                1, TimeUnit.HOURS) // Flex interval
                .setConstraints(constraints)
                .addTag(SecurityCheckWorker.WORK_NAME)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                SecurityCheckWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                securityCheckRequest
        );
    }

    /**
     * Cancel scheduled security checks
     */
    public void cancelSecurityChecks() {
        WorkManager.getInstance(context).cancelUniqueWork(SecurityCheckWorker.WORK_NAME);
    }

    /**
     * Run an immediate security check
     */
    public void runImmediateCheck() {
        androidx.work.OneTimeWorkRequest immediateCheck = new androidx.work.OneTimeWorkRequest.Builder(
                SecurityCheckWorker.class)
                .addTag("immediate_security_check")
                .build();

        WorkManager.getInstance(context).enqueue(immediateCheck);
    }

    // ==================== PREFERENCES ====================

    public boolean isSecurityAlertsEnabled() {
        return prefs.getBoolean(KEY_SECURITY_ALERTS, true);
    }

    public void setSecurityAlertsEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SECURITY_ALERTS, enabled).apply();
        scheduleSecurityChecks();
    }

    public boolean isPasswordRemindersEnabled() {
        return prefs.getBoolean(KEY_PASSWORD_REMINDERS, true);
    }

    public void setPasswordRemindersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_PASSWORD_REMINDERS, enabled).apply();
        scheduleSecurityChecks();
    }

    public int getCheckIntervalHours() {
        return prefs.getInt(KEY_CHECK_INTERVAL_HOURS, 24); // Default: daily
    }

    public void setCheckIntervalHours(int hours) {
        prefs.edit().putInt(KEY_CHECK_INTERVAL_HOURS, hours).apply();
        scheduleSecurityChecks();
    }
}
