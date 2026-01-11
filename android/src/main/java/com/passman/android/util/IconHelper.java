package com.passman.android.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import com.passman.android.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for category and credential icons.
 */
public class IconHelper {

    private static final Map<String, Integer> CATEGORY_ICONS = new HashMap<>();
    private static final Map<String, Integer> BRAND_ICONS = new HashMap<>();
    private static final Map<String, Integer> CATEGORY_COLORS = new HashMap<>();

    static {
        // Category icons
        CATEGORY_ICONS.put("login", R.drawable.ic_category_login);
        CATEGORY_ICONS.put("credit card", R.drawable.ic_credit_card);
        CATEGORY_ICONS.put("bank", R.drawable.ic_business);
        CATEGORY_ICONS.put("email", R.drawable.ic_email);
        CATEGORY_ICONS.put("social media", R.drawable.ic_person);
        CATEGORY_ICONS.put("work", R.drawable.ic_business);
        CATEGORY_ICONS.put("gaming", R.drawable.ic_game);
        CATEGORY_ICONS.put("streaming", R.drawable.ic_streaming);
        CATEGORY_ICONS.put("education", R.drawable.ic_education);
        CATEGORY_ICONS.put("shopping", R.drawable.ic_credit_card);
        CATEGORY_ICONS.put("other", R.drawable.ic_key);

        // Brand icons (based on URL or title patterns)
        BRAND_ICONS.put("google", R.drawable.ic_email);
        BRAND_ICONS.put("facebook", R.drawable.ic_person);
        BRAND_ICONS.put("twitter", R.drawable.ic_person);
        BRAND_ICONS.put("instagram", R.drawable.ic_person);
        BRAND_ICONS.put("linkedin", R.drawable.ic_business);
        BRAND_ICONS.put("github", R.drawable.ic_business);
        BRAND_ICONS.put("amazon", R.drawable.ic_credit_card);
        BRAND_ICONS.put("netflix", R.drawable.ic_streaming);
        BRAND_ICONS.put("spotify", R.drawable.ic_streaming);
        BRAND_ICONS.put("microsoft", R.drawable.ic_business);
        BRAND_ICONS.put("apple", R.drawable.ic_key);
        BRAND_ICONS.put("bank", R.drawable.ic_business);

        // Category colors
        CATEGORY_COLORS.put("login", R.color.primary);
        CATEGORY_COLORS.put("credit card", R.color.success);
        CATEGORY_COLORS.put("bank", R.color.info);
        CATEGORY_COLORS.put("email", R.color.secondary);
        CATEGORY_COLORS.put("social media", R.color.favorite);
        CATEGORY_COLORS.put("work", R.color.primary_dark);
        CATEGORY_COLORS.put("gaming", R.color.success);
        CATEGORY_COLORS.put("streaming", R.color.error);
        CATEGORY_COLORS.put("education", R.color.info);
        CATEGORY_COLORS.put("other", R.color.primary);
    }

    /**
     * Get icon resource for a category.
     */
    public static int getCategoryIcon(String category) {
        if (category == null) {
            return R.drawable.ic_key;
        }
        Integer icon = CATEGORY_ICONS.get(category.toLowerCase());
        return icon != null ? icon : R.drawable.ic_key;
    }

    /**
     * Get icon based on title or URL.
     */
    public static int getIconForCredential(String title, String url) {
        String searchText = (title + " " + (url != null ? url : "")).toLowerCase();
        
        for (Map.Entry<String, Integer> entry : BRAND_ICONS.entrySet()) {
            if (searchText.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return R.drawable.ic_key;
    }

    /**
     * Get color resource for a category.
     */
    public static int getCategoryColor(String category) {
        if (category == null) {
            return R.color.primary;
        }
        Integer color = CATEGORY_COLORS.get(category.toLowerCase());
        return color != null ? color : R.color.primary;
    }

    /**
     * Get color based on password strength.
     */
    public static int getStrengthColor(int strength) {
        if (strength < Constants.STRENGTH_WEAK_MAX) {
            return R.color.strength_weak;
        } else if (strength < Constants.STRENGTH_FAIR_MAX) {
            return R.color.strength_fair;
        } else if (strength < Constants.STRENGTH_GOOD_MAX) {
            return R.color.strength_good;
        } else {
            return R.color.strength_strong;
        }
    }

    /**
     * Get strength label.
     */
    public static String getStrengthLabel(Context context, int strength) {
        if (strength < Constants.STRENGTH_WEAK_MAX) {
            return context.getString(R.string.strength_weak);
        } else if (strength < Constants.STRENGTH_FAIR_MAX) {
            return context.getString(R.string.strength_fair);
        } else if (strength < Constants.STRENGTH_GOOD_MAX) {
            return context.getString(R.string.strength_good);
        } else if (strength < Constants.STRENGTH_STRONG_MAX) {
            return context.getString(R.string.strength_strong);
        } else {
            return context.getString(R.string.strength_very_strong);
        }
    }

    /**
     * Get drawable for category.
     */
    public static Drawable getCategoryDrawable(Context context, String category) {
        int iconRes = getCategoryIcon(category);
        return ContextCompat.getDrawable(context, iconRes);
    }

    /**
     * Get all available categories.
     */
    public static String[] getCategories() {
        return new String[]{
            Constants.CATEGORY_LOGIN,
            Constants.CATEGORY_CREDIT_CARD,
            Constants.CATEGORY_BANK,
            Constants.CATEGORY_EMAIL,
            Constants.CATEGORY_SOCIAL,
            Constants.CATEGORY_WORK,
            Constants.CATEGORY_GAMING,
            Constants.CATEGORY_STREAMING,
            Constants.CATEGORY_SHOPPING,
            Constants.CATEGORY_OTHER
        };
    }
}
