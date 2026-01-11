package com.passman.android.data.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Room Entity representing a card (Credit, Debit, ID, etc.)
 */
@Entity(tableName = "cards")
public class CardEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "card_name")
    private String cardName;

    @ColumnInfo(name = "card_type")
    private String cardType; // CREDIT, DEBIT, ID, PASSPORT, DRIVERS_LICENSE, INSURANCE, MEMBERSHIP, OTHER

    @ColumnInfo(name = "card_number")
    private String cardNumber; // Encrypted

    @ColumnInfo(name = "cardholder_name")
    private String cardholderName;

    @ColumnInfo(name = "expiry_month")
    private int expiryMonth;

    @ColumnInfo(name = "expiry_year")
    private int expiryYear;

    @ColumnInfo(name = "cvv")
    private String cvv; // Encrypted

    @ColumnInfo(name = "pin")
    private String pin; // Encrypted

    @ColumnInfo(name = "issuer")
    private String issuer; // Bank/Organization name

    @ColumnInfo(name = "billing_address")
    private String billingAddress;

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "color")
    private String color; // Card background color

    @ColumnInfo(name = "is_favorite")
    private boolean isFavorite;

    @ColumnInfo(name = "front_image_path")
    private String frontImagePath; // Path to encrypted front image/PDF

    @ColumnInfo(name = "back_image_path")
    private String backImagePath; // Path to encrypted back image/PDF

    @ColumnInfo(name = "renewal_reminder_days")
    private int renewalReminderDays; // Days before expiry to remind

    @ColumnInfo(name = "last_reminder_sent")
    private long lastReminderSent;

    @ColumnInfo(name = "created_at")
    private long createdAt;

    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }

    public String getCardType() { return cardType; }
    public void setCardType(String cardType) { this.cardType = cardType; }

    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

    public String getCardholderName() { return cardholderName; }
    public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

    public int getExpiryMonth() { return expiryMonth; }
    public void setExpiryMonth(int expiryMonth) { this.expiryMonth = expiryMonth; }

    public int getExpiryYear() { return expiryYear; }
    public void setExpiryYear(int expiryYear) { this.expiryYear = expiryYear; }

    public String getCvv() { return cvv; }
    public void setCvv(String cvv) { this.cvv = cvv; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public String getIssuer() { return issuer; }
    public void setIssuer(String issuer) { this.issuer = issuer; }

    public String getBillingAddress() { return billingAddress; }
    public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public String getFrontImagePath() { return frontImagePath; }
    public void setFrontImagePath(String frontImagePath) { this.frontImagePath = frontImagePath; }

    public String getBackImagePath() { return backImagePath; }
    public void setBackImagePath(String backImagePath) { this.backImagePath = backImagePath; }

    public int getRenewalReminderDays() { return renewalReminderDays; }
    public void setRenewalReminderDays(int renewalReminderDays) { this.renewalReminderDays = renewalReminderDays; }

    public long getLastReminderSent() { return lastReminderSent; }
    public void setLastReminderSent(long lastReminderSent) { this.lastReminderSent = lastReminderSent; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isExpired() {
        java.util.Calendar now = java.util.Calendar.getInstance();
        int currentYear = now.get(java.util.Calendar.YEAR);
        int currentMonth = now.get(java.util.Calendar.MONTH) + 1;
        
        if (expiryYear < currentYear) return true;
        if (expiryYear == currentYear && expiryMonth < currentMonth) return true;
        return false;
    }

    public boolean isExpiringSoon(int daysThreshold) {
        if (expiryMonth == 0 || expiryYear == 0) return false;
        
        java.util.Calendar expiry = java.util.Calendar.getInstance();
        expiry.set(java.util.Calendar.YEAR, expiryYear);
        expiry.set(java.util.Calendar.MONTH, expiryMonth - 1);
        expiry.set(java.util.Calendar.DAY_OF_MONTH, expiry.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
        
        java.util.Calendar threshold = java.util.Calendar.getInstance();
        threshold.add(java.util.Calendar.DAY_OF_MONTH, daysThreshold);
        
        return expiry.before(threshold) && !isExpired();
    }

    public String getFormattedExpiry() {
        if (expiryMonth == 0 || expiryYear == 0) return "N/A";
        return String.format("%02d/%d", expiryMonth, expiryYear % 100);
    }

    public String getMaskedCardNumber() {
        if (cardNumber == null || cardNumber.length() < 4) return "****";
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }

    /**
     * Card type enum values
     */
    public enum CardType {
        CREDIT("Credit Card", "💳"),
        DEBIT("Debit Card", "💳"),
        ID("ID Card", "🪪"),
        PASSPORT("Passport", "🛂"),
        DRIVERS_LICENSE("Driver's License", "🚗"),
        INSURANCE("Insurance Card", "🏥"),
        MEMBERSHIP("Membership Card", "🎫"),
        STUDENT("Student ID", "🎓"),
        EMPLOYEE("Employee ID", "👔"),
        OTHER("Other", "📇");

        private final String displayName;
        private final String emoji;

        CardType(String displayName, String emoji) {
            this.displayName = displayName;
            this.emoji = emoji;
        }

        public String getDisplayName() { return displayName; }
        public String getEmoji() { return emoji; }
    }
}
