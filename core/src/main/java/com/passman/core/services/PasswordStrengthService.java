package com.passman.core.services;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Centralized service for password strength evaluation with personal information detection.
 * Checks for common weaknesses including:
 * - Username usage
 * - Email (own or suspected patterns)
 * - Phone numbers (own or suspected patterns)
 * - Birthdays and date formats
 */
public class PasswordStrengthService {

    // Date pattern matchers
    private static final Pattern DATE_PATTERNS = Pattern.compile(
            // Common date formats
            "(?i)" +
                    "(\\d{1,2}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4})|" +  // DD/MM/YYYY, MM/DD/YYYY, DD-MM-YY
                    "(\\d{4}[/\\-.]\\d{1,2}[/\\-.]\\d{1,2})|" +     // YYYY/MM/DD, YYYY-MM-DD
                    "(\\d{2}\\d{2}\\d{2,4})|" +                      // DDMMYYYY, MMDDYYYY (continuous)
                    "(\\d{4}\\d{2}\\d{2})|" +                        // YYYYMMDD
                    "((?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\s*\\d{1,2})|" + // Month DD
                    "(\\d{1,2}\\s*(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec))|" + // DD Month
                    "((?:19|20)\\d{2})"                             // Years 1900-2099
    );

    // Phone number pattern matchers
    private static final Pattern PHONE_PATTERNS = Pattern.compile(
            // Various phone number formats
            "(\\+?\\d{1,3}[\\s.-]?)?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}|" + // US format
                    "\\d{10,11}|" +                                  // 10-11 consecutive digits
                    "(\\d{3}[\\s.-]){2}\\d{4}|" +                   // 123-456-7890
                    "\\d{4,5}[\\s.-]?\\d{4,6}"                      // Various international formats
    );

    // Email pattern for extracting local part
    private static final Pattern EMAIL_LOCAL_PART = Pattern.compile(
            "^([^@]+)@"
    );

    // Common email-like patterns in passwords
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );

    /**
     * Result object containing score and warnings
     */
    public static class StrengthResult {
        private int score;
        private final List<String> warnings;
        private boolean containsPersonalInfo;

        public StrengthResult() {
            this.score = 0;
            this.warnings = new ArrayList<>();
            this.containsPersonalInfo = false;
        }

        public int getScore() { return Math.max(0, Math.min(100, score)); }
        public void setScore(int score) { this.score = score; }
        public void addScore(int points) { this.score += points; }
        public void subtractScore(int points) { this.score -= points; }

        public List<String> getWarnings() { return warnings; }
        public void addWarning(String warning) { this.warnings.add(warning); }
        public boolean hasWarnings() { return !warnings.isEmpty(); }

        public boolean containsPersonalInfo() { return containsPersonalInfo; }
        public void setContainsPersonalInfo(boolean value) { this.containsPersonalInfo = value; }

        public String getStrengthLabel() {
            int finalScore = getScore();
            if (finalScore >= 75) return "Strong";
            else if (finalScore >= 50) return "Medium";
            else return "Weak";
        }
    }

    /**
     * Context object containing personal information to check against
     */
    public static class PersonalInfoContext {
        private String username;
        private String email;
        private String phoneNumber;
        private String birthday; // Format: any date string
        private List<String> additionalEmails;
        private List<String> additionalPhones;

        public PersonalInfoContext() {
            this.additionalEmails = new ArrayList<>();
            this.additionalPhones = new ArrayList<>();
        }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

        public String getBirthday() { return birthday; }
        public void setBirthday(String birthday) { this.birthday = birthday; }

        public List<String> getAdditionalEmails() { return additionalEmails; }
        public void addAdditionalEmail(String email) { this.additionalEmails.add(email); }

        public List<String> getAdditionalPhones() { return additionalPhones; }
        public void addAdditionalPhone(String phone) { this.additionalPhones.add(phone); }
    }

    /**
     * Calculate password strength without personal info context (basic scoring)
     */
    public StrengthResult calculateStrength(String password) {
        return calculateStrength(password, null);
    }

    /**
     * Calculate password strength with personal info context
     */
    public StrengthResult calculateStrength(String password, PersonalInfoContext context) {
        StrengthResult result = new StrengthResult();

        if (password == null || password.isEmpty()) {
            result.addWarning("Password is empty");
            return result;
        }

        // Base score calculation
        calculateBaseScore(password, result);

        // Check for personal information
        if (context != null) {
            checkPersonalInfo(password, context, result);
        }

        // Always check for date patterns (suspected birthday or dates)
        checkDatePatterns(password, result);

        // Always check for phone patterns (suspected phone numbers)
        checkPhonePatterns(password, result);

        // Always check for email patterns
        checkEmailPatterns(password, result);

        return result;
    }

    /**
     * Calculate base score from password characteristics
     */
    private void calculateBaseScore(String password, StrengthResult result) {
        int score = 0;

        // Length scoring
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 25;
        if (password.length() >= 16) score += 10;

        // Character diversity
        if (password.matches(".*[A-Z].*")) score += 15;
        if (password.matches(".*[a-z].*")) score += 15;
        if (password.matches(".*[0-9].*")) score += 10;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*")) score += 10;

        // Bonus for longer passwords
        if (password.length() >= 20) score += 5;

        result.setScore(Math.min(100, score));
    }

    /**
     * Check for personal information in password
     */
    private void checkPersonalInfo(String password, PersonalInfoContext context, StrengthResult result) {
        String passwordLower = password.toLowerCase();

        // Check username
        if (context.getUsername() != null && !context.getUsername().isEmpty()) {
            String usernameLower = context.getUsername().toLowerCase();
            if (usernameLower.length() >= 3 && passwordLower.contains(usernameLower)) {
                result.subtractScore(25);
                result.addWarning("⚠️ Password contains your username");
                result.setContainsPersonalInfo(true);
            }
        }

        // Check email (own)
        if (context.getEmail() != null && !context.getEmail().isEmpty()) {
            checkEmailInPassword(password, context.getEmail(), result, "your email");
        }

        // Check additional emails
        for (String email : context.getAdditionalEmails()) {
            if (email != null && !email.isEmpty()) {
                checkEmailInPassword(password, email, result, "a known email");
            }
        }

        // Check phone number (own)
        if (context.getPhoneNumber() != null && !context.getPhoneNumber().isEmpty()) {
            checkPhoneInPassword(password, context.getPhoneNumber(), result, "your phone number");
        }

        // Check additional phones
        for (String phone : context.getAdditionalPhones()) {
            if (phone != null && !phone.isEmpty()) {
                checkPhoneInPassword(password, phone, result, "a known phone number");
            }
        }

        // Check birthday
        if (context.getBirthday() != null && !context.getBirthday().isEmpty()) {
            checkBirthdayInPassword(password, context.getBirthday(), result);
        }
    }

    /**
     * Check if email or its parts are in password
     */
    private void checkEmailInPassword(String password, String email, StrengthResult result, String description) {
        String passwordLower = password.toLowerCase();
        String emailLower = email.toLowerCase();

        // Check full email
        if (passwordLower.contains(emailLower)) {
            result.subtractScore(30);
            result.addWarning("⚠️ Password contains " + description);
            result.setContainsPersonalInfo(true);
            return;
        }

        // Check email local part (before @)
        var matcher = EMAIL_LOCAL_PART.matcher(emailLower);
        if (matcher.find()) {
            String localPart = matcher.group(1);
            if (localPart.length() >= 3 && passwordLower.contains(localPart)) {
                result.subtractScore(20);
                result.addWarning("⚠️ Password contains email username part");
                result.setContainsPersonalInfo(true);
            }
        }
    }

    /**
     * Check if phone number or its parts are in password
     */
    private void checkPhoneInPassword(String password, String phone, StrengthResult result, String description) {
        // Remove common phone formatting
        String phoneDigits = phone.replaceAll("[^0-9]", "");
        String passwordDigits = password.replaceAll("[^0-9]", "");

        if (phoneDigits.length() >= 4) {
            // Check for consecutive digit sequences from phone
            for (int len = Math.min(phoneDigits.length(), 10); len >= 4; len--) {
                for (int i = 0; i <= phoneDigits.length() - len; i++) {
                    String segment = phoneDigits.substring(i, i + len);
                    if (passwordDigits.contains(segment)) {
                        result.subtractScore(20);
                        result.addWarning("⚠️ Password contains " + description + " digits");
                        result.setContainsPersonalInfo(true);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Check if birthday components are in password
     */
    private void checkBirthdayInPassword(String password, String birthday, StrengthResult result) {
        String passwordLower = password.toLowerCase();

        // Extract digits from birthday
        String birthdayDigits = birthday.replaceAll("[^0-9]", "");
        String passwordDigits = password.replaceAll("[^0-9]", "");

        // Check for full birthday in various formats
        if (birthdayDigits.length() >= 4) {
            // Check for consecutive sequences
            for (int len = Math.min(birthdayDigits.length(), 8); len >= 4; len--) {
                for (int i = 0; i <= birthdayDigits.length() - len; i++) {
                    String segment = birthdayDigits.substring(i, i + len);
                    if (passwordDigits.contains(segment)) {
                        result.subtractScore(25);
                        result.addWarning("⚠️ Password contains your birthday");
                        result.setContainsPersonalInfo(true);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Check for any date patterns in password (suspected dates)
     */
    private void checkDatePatterns(String password, StrengthResult result) {
        // Skip if we already flagged birthday
        if (result.getWarnings().stream().anyMatch(w -> w.contains("birthday"))) {
            return;
        }

        if (DATE_PATTERNS.matcher(password).find()) {
            // Only penalize and warn if not already warned about dates
            if (result.getWarnings().stream().noneMatch(w -> w.contains("date"))) {
                result.subtractScore(15);
                result.addWarning("⚠️ Password contains date patterns (avoid using dates)");
                result.setContainsPersonalInfo(true);
            }
        }
    }

    /**
     * Check for phone number patterns in password (suspected phone numbers)
     */
    private void checkPhonePatterns(String password, StrengthResult result) {
        // Skip if we already flagged phone
        if (result.getWarnings().stream().anyMatch(w -> w.contains("phone"))) {
            return;
        }

        if (PHONE_PATTERNS.matcher(password).find()) {
            result.subtractScore(15);
            result.addWarning("⚠️ Password contains phone number patterns");
            result.setContainsPersonalInfo(true);
        }
    }

    /**
     * Check for email patterns in password (suspected email addresses)
     */
    private void checkEmailPatterns(String password, StrengthResult result) {
        // Skip if we already flagged email
        if (result.getWarnings().stream().anyMatch(w -> w.contains("email"))) {
            return;
        }

        if (EMAIL_PATTERN.matcher(password).find()) {
            result.subtractScore(20);
            result.addWarning("⚠️ Password contains an email address pattern");
            result.setContainsPersonalInfo(true);
        }
    }

    /**
     * Get score only (for backward compatibility)
     */
    public int getScore(String password) {
        return calculateStrength(password).getScore();
    }

    /**
     * Get score with context
     */
    public int getScore(String password, PersonalInfoContext context) {
        return calculateStrength(password, context).getScore();
    }

    /**
     * Get strength label only
     */
    public String getStrengthLabel(String password) {
        return calculateStrength(password).getStrengthLabel();
    }

    /**
     * Get strength label with context
     */
    public String getStrengthLabel(String password, PersonalInfoContext context) {
        return calculateStrength(password, context).getStrengthLabel();
    }
}
