package com.passman.android.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.passman.android.R;

/**
 * Utility class for clipboard operations with auto-clear functionality.
 */
public class ClipboardUtils {

    private static final long DEFAULT_CLEAR_DELAY = 30000; // 30 seconds
    private static Handler handler = new Handler(Looper.getMainLooper());
    private static Runnable clearRunnable;

    /**
     * Copy text to clipboard with auto-clear after default delay.
     */
    public static void copyToClipboard(Context context, String label, String text) {
        copyToClipboard(context, label, text, DEFAULT_CLEAR_DELAY, true);
    }

    /**
     * Copy text to clipboard with optional auto-clear.
     */
    public static void copyToClipboard(Context context, String label, String text, 
                                        long clearDelayMs, boolean showToast) {
        ClipboardManager clipboard = (ClipboardManager) 
            context.getSystemService(Context.CLIPBOARD_SERVICE);
        
        if (clipboard == null) return;
        
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        
        if (showToast) {
            Toast.makeText(context, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
        }
        
        // Cancel any pending clear operation
        if (clearRunnable != null) {
            handler.removeCallbacks(clearRunnable);
        }
        
        // Schedule clipboard clear
        if (clearDelayMs > 0) {
            clearRunnable = () -> clearClipboard(context);
            handler.postDelayed(clearRunnable, clearDelayMs);
        }
    }

    /**
     * Clear the clipboard.
     */
    public static void clearClipboard(Context context) {
        ClipboardManager clipboard = (ClipboardManager) 
            context.getSystemService(Context.CLIPBOARD_SERVICE);
        
        if (clipboard == null) return;
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            clipboard.clearPrimaryClip();
        } else {
            ClipData clip = ClipData.newPlainText("", "");
            clipboard.setPrimaryClip(clip);
        }
    }

    /**
     * Copy password to clipboard with security measures.
     */
    public static void copyPassword(Context context, String password) {
        copyToClipboard(context, "Password", password, DEFAULT_CLEAR_DELAY, true);
    }

    /**
     * Copy username to clipboard.
     */
    public static void copyUsername(Context context, String username) {
        copyToClipboard(context, "Username", username, 0, true); // No auto-clear for usernames
    }
}
