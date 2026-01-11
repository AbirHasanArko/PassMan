package com.passman.android.notification;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.passman.android.R;
import com.passman.android.ui.main.MainActivity;

/**
 * Manages all notifications for the PassMan app
 */
public class NotificationHelper {

    // Notification Channel IDs
    public static final String CHANNEL_SECURITY = "security_alerts";
    public static final String CHANNEL_REMINDERS = "password_reminders";
    public static final String CHANNEL_GENERAL = "general";

    // Notification IDs
    public static final int NOTIFICATION_WEAK_PASSWORDS = 1001;
    public static final int NOTIFICATION_OLD_PASSWORDS = 1002;
    public static final int NOTIFICATION_BREACH_ALERT = 1003;
    public static final int NOTIFICATION_BACKUP_REMINDER = 1004;
    public static final int NOTIFICATION_PASSWORD_EXPIRY = 1005;

    private final Context context;
    private final NotificationManagerCompat notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        createNotificationChannels();
    }

    /**
     * Create notification channels for Android O and above
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);

            // Security Alerts Channel (High Priority)
            NotificationChannel securityChannel = new NotificationChannel(
                    CHANNEL_SECURITY,
                    "Security Alerts",
                    NotificationManager.IMPORTANCE_HIGH
            );
            securityChannel.setDescription("Important security notifications about your passwords");
            securityChannel.enableVibration(true);
            securityChannel.setShowBadge(true);
            manager.createNotificationChannel(securityChannel);

            // Password Reminders Channel (Default Priority)
            NotificationChannel remindersChannel = new NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Password Reminders",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            remindersChannel.setDescription("Reminders to update old passwords");
            remindersChannel.setShowBadge(true);
            manager.createNotificationChannel(remindersChannel);

            // General Channel (Low Priority)
            NotificationChannel generalChannel = new NotificationChannel(
                    CHANNEL_GENERAL,
                    "General",
                    NotificationManager.IMPORTANCE_LOW
            );
            generalChannel.setDescription("General app notifications");
            manager.createNotificationChannel(generalChannel);
        }
    }

    /**
     * Check if notifications are enabled
     */
    public boolean areNotificationsEnabled() {
        return notificationManager.areNotificationsEnabled();
    }

    /**
     * Show a test notification to verify notifications are working
     */
    public void showTestNotification() {
        if (!hasNotificationPermission()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GENERAL)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.test_notification_title))
                .setContentText(context.getString(R.string.test_notification_message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getColor(R.color.primary));

        notificationManager.notify(999, builder.build());
    }

    /**
     * Check if we have notification permission (Android 13+)
     */
    public boolean hasNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, 
                    Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        }
        return true;
    }

    /**
     * Show weak passwords alert
     */
    public void showWeakPasswordsAlert(int count) {
        if (!hasNotificationPermission()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("show_weak_passwords", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SECURITY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Weak Passwords Detected")
                .setContentText(count + " password(s) need to be strengthened")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("You have " + count + " weak password(s) that could be easily compromised. " +
                                "Tap to review and update them for better security."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getColor(R.color.warning));

        notificationManager.notify(NOTIFICATION_WEAK_PASSWORDS, builder.build());
    }

    /**
     * Show old passwords reminder
     */
    public void showOldPasswordsReminder(int count, int daysOld) {
        if (!hasNotificationPermission()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("show_old_passwords", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Password Update Recommended")
                .setContentText(count + " password(s) haven't been changed in " + daysOld + "+ days")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(count + " of your passwords haven't been updated in over " + daysOld + " days. " +
                                "Regular password updates help keep your accounts secure."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getColor(R.color.info));

        notificationManager.notify(NOTIFICATION_OLD_PASSWORDS, builder.build());
    }

    /**
     * Show breach alert notification
     */
    public void showBreachAlert(String credentialTitle) {
        if (!hasNotificationPermission()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("show_breached", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_SECURITY)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("⚠️ Security Alert")
                .setContentText("Password for \"" + credentialTitle + "\" may be compromised")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Your password for \"" + credentialTitle + "\" was found in a data breach. " +
                                "We strongly recommend changing this password immediately."))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getColor(R.color.error));

        notificationManager.notify(NOTIFICATION_BREACH_ALERT, builder.build());
    }

    /**
     * Show backup reminder notification
     */
    public void showBackupReminder() {
        if (!hasNotificationPermission()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("show_settings", true);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Backup Your Vault")
                .setContentText("It's been a while since your last backup")
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Protect your passwords by creating a backup. " +
                                "Regular backups ensure you never lose access to your accounts."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getColor(R.color.secondary));

        notificationManager.notify(NOTIFICATION_BACKUP_REMINDER, builder.build());
    }

    /**
     * Show password expiry warning
     */
    public void showPasswordExpiryWarning(String credentialTitle, int daysUntilExpiry) {
        if (!hasNotificationPermission()) return;

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String message = daysUntilExpiry <= 0 
                ? "Password for \"" + credentialTitle + "\" has expired"
                : "Password for \"" + credentialTitle + "\" expires in " + daysUntilExpiry + " days";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_REMINDERS)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Password Expiry Notice")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(context.getColor(R.color.warning));

        notificationManager.notify(NOTIFICATION_PASSWORD_EXPIRY, builder.build());
    }

    /**
     * Cancel a specific notification
     */
    public void cancelNotification(int notificationId) {
        notificationManager.cancel(notificationId);
    }

    /**
     * Cancel all notifications
     */
    public void cancelAllNotifications() {
        notificationManager.cancelAll();
    }
}
