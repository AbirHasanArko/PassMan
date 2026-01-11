package com.passman.android.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Utility class for date formatting and calculations.
 */
public class DateUtils {

    private static final SimpleDateFormat DATE_FORMAT = 
        new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
    
    private static final SimpleDateFormat DATE_TIME_FORMAT = 
        new SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault());
    
    private static final SimpleDateFormat SHORT_DATE_FORMAT = 
        new SimpleDateFormat("MMM d", Locale.getDefault());

    /**
     * Format date to readable string (e.g., "Jan 5, 2026").
     */
    public static String formatDate(long timestamp) {
        return DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format date with time (e.g., "Jan 5, 2026 at 3:45 PM").
     */
    public static String formatDateTime(long timestamp) {
        return DATE_TIME_FORMAT.format(new Date(timestamp));
    }

    /**
     * Format short date (e.g., "Jan 5").
     */
    public static String formatShortDate(long timestamp) {
        return SHORT_DATE_FORMAT.format(new Date(timestamp));
    }

    /**
     * Calculate days since a timestamp.
     */
    public static int daysSince(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        return (int) TimeUnit.MILLISECONDS.toDays(diff);
    }

    /**
     * Get a relative time string (e.g., "5 days ago", "Just now").
     */
    public static String getRelativeTime(long timestamp) {
        long diff = System.currentTimeMillis() - timestamp;
        long days = TimeUnit.MILLISECONDS.toDays(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        
        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else if (hours < 24) {
            return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        } else if (days < 7) {
            return days + " day" + (days == 1 ? "" : "s") + " ago";
        } else if (days < 30) {
            long weeks = days / 7;
            return weeks + " week" + (weeks == 1 ? "" : "s") + " ago";
        } else if (days < 365) {
            long months = days / 30;
            return months + " month" + (months == 1 ? "" : "s") + " ago";
        } else {
            long years = days / 365;
            return years + " year" + (years == 1 ? "" : "s") + " ago";
        }
    }

    /**
     * Check if password is old (more than 90 days).
     */
    public static boolean isPasswordOld(long lastModified) {
        return daysSince(lastModified) > 90;
    }

    /**
     * Get password age description.
     */
    public static String getPasswordAgeDescription(long lastModified) {
        int days = daysSince(lastModified);
        
        if (days == 0) {
            return "Updated today";
        } else if (days == 1) {
            return "Updated yesterday";
        } else if (days < 30) {
            return days + " days old";
        } else if (days < 365) {
            int months = days / 30;
            return months + " month" + (months == 1 ? "" : "s") + " old";
        } else {
            int years = days / 365;
            return years + " year" + (years == 1 ? "" : "s") + " old";
        }
    }
}
