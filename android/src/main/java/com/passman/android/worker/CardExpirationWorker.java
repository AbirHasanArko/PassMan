package com.passman.android.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.passman.android.R;
import com.passman.android.data.database.PassManDatabase;
import com.passman.android.data.entity.CardEntity;
import com.passman.android.ui.cards.CardsActivity;

import java.util.List;

/**
 * Worker that checks for expiring cards and sends notifications
 */
public class CardExpirationWorker extends Worker {

    public static final String WORK_NAME = "card_expiration_check";
    public static final String CHANNEL_ID = "card_expiration_channel";
    private static final int NOTIFICATION_ID_BASE = 2000;

    public CardExpirationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        createNotificationChannel();

        try {
            PassManDatabase db = PassManDatabase.getInstance(getApplicationContext());
            List<CardEntity> cards = db.cardDao().getCardsWithExpirySync();

            int notificationCount = 0;
            long currentTime = System.currentTimeMillis();
            long oneDayMillis = 24 * 60 * 60 * 1000L;

            for (CardEntity card : cards) {
                // Skip if no reminder set
                if (card.getRenewalReminderDays() <= 0) {
                    continue;
                }

                // Check if already notified today
                if (card.getLastReminderSent() > 0 && 
                    (currentTime - card.getLastReminderSent()) < oneDayMillis) {
                    continue;
                }

                // Check expiration status
                if (card.isExpired()) {
                    sendExpirationNotification(card, true);
                    updateLastReminderSent(card);
                    notificationCount++;
                } else if (card.isExpiringSoon(card.getRenewalReminderDays())) {
                    sendExpirationNotification(card, false);
                    updateLastReminderSent(card);
                    notificationCount++;
                }
            }

            return Result.success();
        } catch (Exception e) {
            return Result.failure();
        }
    }

    private void sendExpirationNotification(CardEntity card, boolean isExpired) {
        Context context = getApplicationContext();

        // Create intent to open cards activity
        Intent intent = new Intent(context, CardsActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, (int) card.getId(), intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        String title;
        String message;
        CardEntity.CardType cardType = getCardType(card.getCardType());

        if (isExpired) {
            title = cardType.getEmoji() + " Card Expired!";
            message = card.getCardName() + " expired on " + card.getFormattedExpiry();
        } else {
            title = cardType.getEmoji() + " Card Expiring Soon";
            message = card.getCardName() + " expires on " + card.getFormattedExpiry();
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setCategory(NotificationCompat.CATEGORY_REMINDER);

        try {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID_BASE + (int) card.getId(), builder.build());
        } catch (SecurityException e) {
            // Notification permission not granted
        }
    }

    private void updateLastReminderSent(CardEntity card) {
        try {
            card.setLastReminderSent(System.currentTimeMillis());
            PassManDatabase db = PassManDatabase.getInstance(getApplicationContext());
            db.cardDao().update(card);
        } catch (Exception ignored) {}
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Card Expiration Reminders";
            String description = "Notifications for card expiration and renewal reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            
            NotificationManager notificationManager = 
                    getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private CardEntity.CardType getCardType(String typeName) {
        if (typeName == null) return CardEntity.CardType.OTHER;
        try {
            return CardEntity.CardType.valueOf(typeName);
        } catch (Exception e) {
            return CardEntity.CardType.OTHER;
        }
    }
}
