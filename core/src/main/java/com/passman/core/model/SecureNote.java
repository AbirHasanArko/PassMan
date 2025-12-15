package com.passman.core.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain model for secure encrypted notes
 */
public class SecureNote {
    private Long id;
    private String title;
    private byte[] encryptedContent;
    private byte[] encryptionIV;
    private NoteCategory category;
    private String tags;
    private boolean isFavorite;
    private boolean hasAttachments;
    private String colorCode;
    private LocalDateTime createdAt;
    private LocalDateTime lastModified;

    // Transient field for decrypted content (not stored in DB)
    private transient String content;
    private transient List<NoteAttachment> attachments;

    public enum NoteCategory {
        PERSONAL("Personal", "#4A90E2"),
        WORK("Work", "#7B68EE"),
        FINANCIAL("Financial", "#2ECC71"),
        MEDICAL("Medical", "#E74C3C"),
        TECHNICAL("Technical", "#F39C12"),
        OTHER("Other", "#95A5A6");

        private final String displayName;
        private final String defaultColor;

        NoteCategory(String displayName, String defaultColor) {
            this.displayName = displayName;
            this.defaultColor = defaultColor;
        }

        public String getDisplayName() { return displayName; }
        public String getDefaultColor() { return defaultColor; }
    }

    public SecureNote() {
        this.createdAt = LocalDateTime. now();
        this.lastModified = LocalDateTime.now();
        this.category = NoteCategory.PERSONAL;
        this.attachments = new ArrayList<>();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public byte[] getEncryptedContent() { return encryptedContent; }
    public void setEncryptedContent(byte[] encryptedContent) { this.encryptedContent = encryptedContent; }

    public byte[] getEncryptionIV() { return encryptionIV; }
    public void setEncryptionIV(byte[] encryptionIV) { this.encryptionIV = encryptionIV; }

    public NoteCategory getCategory() { return category; }
    public void setCategory(NoteCategory category) { this.category = category; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    public boolean isHasAttachments() { return hasAttachments; }
    public void setHasAttachments(boolean hasAttachments) { this.hasAttachments = hasAttachments; }

    public String getColorCode() { return colorCode; }
    public void setColorCode(String colorCode) { this.colorCode = colorCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastModified() { return lastModified; }
    public void setLastModified(LocalDateTime lastModified) { this.lastModified = lastModified; }

    // Transient fields
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public List<NoteAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<NoteAttachment> attachments) { this.attachments = attachments; }
}