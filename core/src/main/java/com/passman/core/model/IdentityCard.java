package com.passman.core.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

/**
 * Domain model for digital identity cards (encrypted)
 */
public class IdentityCard {
    private Long id;
    private CardType cardType;
    private String cardName;
    private byte[] encryptedData;
    private byte[] encryptionIV;
    private String cardNumberLast4;
    private String issuingCountry;
    private String issuingAuthority;
    private LocalDate issueDate;
    private LocalDate expiryDate;
    private boolean hasPhoto;
    private byte[] encryptedPhoto;
    private byte[] photoEncryptionIV;
    private boolean isExpired;
    private String tags;
    private String colorCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;

    // Transient field for decrypted data
    private transient Map<String, String> cardData;

    public enum CardType {
        PASSPORT("Passport", "🛂", new String[]{"passportNumber", "fullName", "nationality", "dateOfBirth", "placeOfBirth", "sex"}),
        DRIVERS_LICENSE("Driver's License", "🪪", new String[]{"licenseNumber", "fullName", "dateOfBirth", "address", "class", "restrictions"}),
        NATIONAL_ID("National ID", "🆔", new String[]{"idNumber", "fullName", "dateOfBirth", "address", "nationality"}),
        CREDIT_CARD("Credit Card", "💳", new String[]{"cardNumber", "cardholderName", "cvv", "pin", "billingAddress"}),
        DEBIT_CARD("Debit Card", "💳", new String[]{"cardNumber", "cardholderName", "cvv", "pin", "bankName"}),
        BANK_ACCOUNT("Bank Account", "🏦", new String[]{"accountNumber", "accountType", "routingNumber", "swiftCode", "bankName", "branchAddress"}),
        INSURANCE("Insurance Card", "🏥", new String[]{"policyNumber", "groupNumber", "memberName", "memberId", "providerName", "providerPhone"}),
        SSN("SSN/Tax ID", "📋", new String[]{"ssn", "fullName", "dateOfBirth"}),
        MEMBERSHIP("Membership Card", "🎫", new String[]{"memberNumber", "memberName", "organizationName"}),
        OTHER("Other", "📇", new String[]{"customField1", "customField2", "customField3"});

        private final String displayName;
        private final String icon;
        private final String[] fields;

        CardType(String displayName, String icon, String[] fields) {
            this. displayName = displayName;
            this.icon = icon;
            this.fields = fields;
        }

        public String getDisplayName() { return displayName; }
        public String getIcon() { return icon; }
        public String[] getFields() { return fields; }
    }

    public IdentityCard() {
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime. now();
        this.cardData = new HashMap<>();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public CardType getCardType() { return cardType; }
    public void setCardType(CardType cardType) { this.cardType = cardType; }

    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }

    public byte[] getEncryptedData() { return encryptedData; }
    public void setEncryptedData(byte[] encryptedData) { this.encryptedData = encryptedData; }

    public byte[] getEncryptionIV() { return encryptionIV; }
    public void setEncryptionIV(byte[] encryptionIV) { this.encryptionIV = encryptionIV; }

    public String getCardNumberLast4() { return cardNumberLast4; }
    public void setCardNumberLast4(String cardNumberLast4) { this.cardNumberLast4 = cardNumberLast4; }

    public String getIssuingCountry() { return issuingCountry; }
    public void setIssuingCountry(String issuingCountry) { this.issuingCountry = issuingCountry; }

    public String getIssuingAuthority() { return issuingAuthority; }
    public void setIssuingAuthority(String issuingAuthority) { this.issuingAuthority = issuingAuthority; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
        this.isExpired = expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isHasPhoto() { return hasPhoto; }
    public void setHasPhoto(boolean hasPhoto) { this.hasPhoto = hasPhoto; }

    public byte[] getEncryptedPhoto() { return encryptedPhoto; }
    public void setEncryptedPhoto(byte[] encryptedPhoto) { this.encryptedPhoto = encryptedPhoto; }

    public byte[] getPhotoEncryptionIV() { return photoEncryptionIV; }
    public void setPhotoEncryptionIV(byte[] photoEncryptionIV) { this.photoEncryptionIV = photoEncryptionIV; }

    public boolean isExpired() { return isExpired; }
    public void setExpired(boolean expired) { isExpired = expired; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    // Transient field
    public Map<String, String> getCardData() { return cardData; }
    public void setCardData(Map<String, String> cardData) { this.cardData = cardData; }

    /**
     * Get days until expiry
     */
    public long getDaysUntilExpiry() {
        if (expiryDate == null) return -1;
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    /**
     * Get expiry status with color coding
     */
    public String getExpiryStatus() {
        if (expiryDate == null) return "No Expiry";
        if (isExpired) return "Expired";

        long days = getDaysUntilExpiry();
        if (days <= 30) return "Expires Soon";
        if (days <= 90) return "Expiring";
        return "Valid";
    }
}