package com.passman.core.services;

import com.passman.core.db.DatabaseManager;
import com. passman.core.model. Credential;
import com.passman. core.repository.CredentialRepository;
import com.passman.core.repository.CredentialRepositoryImpl;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java. time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for password analytics and security scoring
 */
public class AnalyticsService {

    private final CredentialRepository credentialRepository;
    private final EncryptionServiceImpl encryptionService;

    public AnalyticsService(DatabaseManager dbManager) {
        this.credentialRepository = new CredentialRepositoryImpl(dbManager);
        this.encryptionService = new EncryptionServiceImpl();
    }

    /**
     * Calculate overall security score (0-100)
     */
    public int calculateSecurityScore(SecretKey masterKey) throws Exception {
        List<Credential> credentials = credentialRepository.findAll();

        if (credentials.isEmpty()) {
            return 100; // No passwords = no risk
        }

        int totalScore = 0;
        int maxScore = credentials.size() * 100;

        for (Credential cred : credentials) {
            totalScore += calculatePasswordScore(cred, masterKey);
        }

        return (totalScore * 100) / maxScore;
    }

    /**
     * Calculate individual password strength score (0-100)
     */
    public int calculatePasswordScore(Credential credential, SecretKey masterKey) throws Exception {
        String password = encryptionService.decryptPassword(
                new String(credential.getEncryptedPassword()),
                masterKey
        );

        int score = 0;

        // Length scoring (max 40 points)
        int length = password.length();
        if (length >= 16) score += 40;
        else if (length >= 12) score += 30;
        else if (length >= 8) score += 20;
        else score += 10;

        // Character variety (max 30 points)
        if (password.matches(".*[A-Z].*")) score += 10;
        if (password.matches(".*[a-z].*")) score += 10;
        if (password.matches(".*[0-9].*")) score += 5;
        if (password.matches(".*[! @#$%^&*()_+\\-=\\[\\]{}|;: ,.<>?].*")) score += 5;

        // Age penalty (max -20 points)
        long daysSinceCreated = ChronoUnit.DAYS.between(credential.getCreatedAt(), LocalDateTime.now());
        if (daysSinceCreated > 365) score -= 20;
        else if (daysSinceCreated > 180) score -= 10;

        // Common patterns penalty (max -20 points)
        if (isCommonPassword(password)) score -= 20;
        if (hasRepeatingCharacters(password)) score -= 10;

        return Math.max(0, Math. min(100, score));
    }

    /**
     * Get password strength distribution
     */
    public Map<String, Integer> getStrengthDistribution(SecretKey masterKey) throws Exception {
        List<Credential> credentials = credentialRepository.findAll();

        int strong = 0;
        int medium = 0;
        int weak = 0;

        for (Credential cred : credentials) {
            int score = calculatePasswordScore(cred, masterKey);
            if (score >= 75) strong++;
            else if (score >= 50) medium++;
            else weak++;
        }

        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("Strong", strong);
        distribution.put("Medium", medium);
        distribution.put("Weak", weak);

        return distribution;
    }

    /**
     * Get password age distribution
     */
    public Map<String, Integer> getAgeDistribution() throws Exception {
        List<Credential> credentials = credentialRepository. findAll();

        int fresh = 0;    // < 90 days
        int old = 0;      // 90-365 days
        int veryOld = 0;  // > 365 days

        for (Credential cred : credentials) {
            long days = ChronoUnit.DAYS.between(cred.getCreatedAt(), LocalDateTime.now());
            if (days < 90) fresh++;
            else if (days < 365) old++;
            else veryOld++;
        }

        Map<String, Integer> distribution = new LinkedHashMap<>();
        distribution.put("Fresh", fresh);
        distribution.put("Old", old);
        distribution.put("Very Old", veryOld);

        return distribution;
    }

    /**
     * Detect password reuse
     */
    public Map<String, List<Credential>> detectPasswordReuse(SecretKey masterKey) throws Exception {
        List<Credential> credentials = credentialRepository.findAll();
        Map<String, List<Credential>> passwordMap = new HashMap<>();

        for (Credential cred : credentials) {
            String password = encryptionService.decryptPassword(
                    new String(cred.getEncryptedPassword()),
                    masterKey
            );

            passwordMap.computeIfAbsent(password, k -> new ArrayList<>()).add(cred);
        }

        // Filter only reused passwords
        return passwordMap.entrySet().stream()
                .filter(entry -> entry. getValue().size() > 1)
                .collect(Collectors. toMap(Map.Entry::getKey, Map. Entry::getValue));
    }

    /**
     * Get top security recommendations
     */
    public List<SecurityRecommendation> getRecommendations(SecretKey masterKey) throws Exception {
        List<SecurityRecommendation> recommendations = new ArrayList<>();
        List<Credential> credentials = credentialRepository.findAll();

        // Check for weak passwords
        int weakCount = 0;
        for (Credential cred : credentials) {
            if (calculatePasswordScore(cred, masterKey) < 50) {
                weakCount++;
            }
        }
        if (weakCount > 0) {
            recommendations.add(new SecurityRecommendation(
                    "🔴 CRITICAL",
                    "Weak Passwords Detected",
                    weakCount + " passwords are weak and should be strengthened immediately.",
                    "high"
            ));
        }

        // Check for old passwords
        int oldCount = 0;
        for (Credential cred : credentials) {
            long days = ChronoUnit. DAYS.between(cred. getCreatedAt(), LocalDateTime.now());
            if (days > 365) oldCount++;
        }
        if (oldCount > 0) {
            recommendations.add(new SecurityRecommendation(
                    "🟡 WARNING",
                    "Old Passwords",
                    oldCount + " passwords are over 1 year old.  Consider updating them.",
                    "medium"
            ));
        }

        // Check for password reuse
        Map<String, List<Credential>> reused = detectPasswordReuse(masterKey);
        if (!reused. isEmpty()) {
            recommendations.add(new SecurityRecommendation(
                    "🟠 IMPORTANT",
                    "Password Reuse Detected",
                    reused.size() + " passwords are being reused across multiple accounts.",
                    "high"
            ));
        }

        // Positive feedback
        if (recommendations.isEmpty()) {
            recommendations.add(new SecurityRecommendation(
                    "✅ EXCELLENT",
                    "Strong Security Posture",
                    "All your passwords are strong and unique.  Keep up the good work!",
                    "low"
            ));
        }

        return recommendations;
    }

    /**
     * Get statistics summary
     */
    public AnalyticsStatistics getStatistics(SecretKey masterKey) throws Exception {
        List<Credential> credentials = credentialRepository.findAll();

        AnalyticsStatistics stats = new AnalyticsStatistics();
        stats.totalPasswords = credentials.size();
        stats.securityScore = calculateSecurityScore(masterKey);

        // Calculate averages
        if (! credentials.isEmpty()) {
            int totalScore = 0;
            long totalAge = 0;

            for (Credential cred : credentials) {
                totalScore += calculatePasswordScore(cred, masterKey);
                totalAge += ChronoUnit.DAYS.between(cred.getCreatedAt(), LocalDateTime.now());
            }

            stats.averagePasswordStrength = totalScore / credentials.size();
            stats.averagePasswordAge = (int) (totalAge / credentials.size());
        }

        // Count reused passwords
        Map<String, List<Credential>> reused = detectPasswordReuse(masterKey);
        stats.reusedPasswordCount = reused.values().stream()
                .mapToInt(List::size)
                .sum();

        return stats;
    }

    // Helper methods

    private boolean isCommonPassword(String password) {
        String[] commonPasswords = {
                "password", "123456", "qwerty", "admin", "letmein",
                "welcome", "monkey", "dragon", "master", "sunshine"
        };

        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasRepeatingCharacters(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) &&
                    password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }

    // Data classes

    public static class SecurityRecommendation {
        public final String severity;
        public final String title;
        public final String description;
        public final String priority;

        public SecurityRecommendation(String severity, String title, String description, String priority) {
            this.severity = severity;
            this.title = title;
            this.description = description;
            this.priority = priority;
        }
    }

    public static class AnalyticsStatistics {
        public int totalPasswords;
        public int securityScore;
        public int averagePasswordStrength;
        public int averagePasswordAge;
        public int reusedPasswordCount;
    }
}