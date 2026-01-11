package com.passman.core.services;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.Credential;
import com.passman.core.repository.CredentialRepository;
import com.passman.core.repository.CredentialRepositoryImpl;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for password analytics and security scoring
 */
public class AnalyticsService {

    private final CredentialRepository credentialRepository;
    private final EncryptionServiceImpl encryptionService;
    private final PasswordStrengthService passwordStrengthService;

    public AnalyticsService(DatabaseManager dbManager) {
        this.credentialRepository = new CredentialRepositoryImpl(dbManager);
        this.encryptionService = new EncryptionServiceImpl();
        this.passwordStrengthService = new PasswordStrengthService();
    }

    /**
     * Calculate overall security score (0-100, using enhanced logic with personal info detection)
     */
    public int calculateSecurityScore(SecretKey masterKey) throws Exception {
        List<Credential> credentials = credentialRepository.findAll();

        if (credentials.isEmpty()) {
            return 100; // No passwords = no risk
        }

        int totalScore = 0;
        int maxScore = credentials.size() * 100;

        for (Credential cred : credentials) {
            String password = decryptPasswordForCredential(cred, masterKey);
            totalScore += calculatePasswordStrengthScoreWithContext(password, cred);
        }

        return (totalScore * 100) / maxScore;
    }

    /**
     * Calculates password strength score for the given credential,
     * using enhanced scoring with personal info detection.
     */
    public int calculatePasswordScore(Credential credential, SecretKey masterKey) throws Exception {
        String password = decryptPasswordForCredential(credential, masterKey);
        return calculatePasswordStrengthScoreWithContext(password, credential);
    }

    /**
     * Calculates strength label ("Strong", "Medium", "Weak") based on freshly computed score.
     * Now includes personal info checking for enhanced security analysis.
     */
    public String calculateStrength(Credential cred, SecretKey masterKey) {
        int score = 0;
        try {
            String password = decryptPasswordForCredential(cred, masterKey);
            PasswordStrengthService.PersonalInfoContext context = buildPersonalInfoContext(cred);
            score = passwordStrengthService.calculateStrength(password, context).getScore();
        } catch (Exception e) {
            score = 0;
        }
        if (score >= 75) return "Strong";
        else if (score >= 50) return "Medium";
        else return "Weak";
    }

    /**
     * Build personal info context from a credential for enhanced strength checking
     */
    private PasswordStrengthService.PersonalInfoContext buildPersonalInfoContext(Credential cred) {
        PasswordStrengthService.PersonalInfoContext context = new PasswordStrengthService.PersonalInfoContext();
        if (cred.getUsername() != null) {
            context.setUsername(cred.getUsername());
        }
        if (cred.getEmail() != null) {
            context.setEmail(cred.getEmail());
        }
        return context;
    }

    /**
     * Password strength scoring with personal info detection.
     */
    private int calculatePasswordStrengthScore(String password) {
        return passwordStrengthService.getScore(password);
    }

    /**
     * Password strength scoring with credential context for personal info detection.
     */
    private int calculatePasswordStrengthScoreWithContext(String password, Credential cred) {
        PasswordStrengthService.PersonalInfoContext context = buildPersonalInfoContext(cred);
        return passwordStrengthService.getScore(password, context);
    }

    /**
     * Helper for decrypting a credential's password, using IV + encrypted buffer.
     */
    private String decryptPasswordForCredential(Credential credential, SecretKey masterKey) throws Exception {
        byte[] iv = credential.getEncryptionIV();
        byte[] encrypted = credential.getEncryptedPassword();
        if (iv != null && encrypted != null && iv.length > 0 && encrypted.length > 0) {
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            String base64 = java.util.Base64.getEncoder().encodeToString(combined);
            return encryptionService.decryptPassword(base64, masterKey);
        }
        return "";
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
            String label = calculateStrength(cred, masterKey);
            switch (label) {
                case "Strong": strong++; break;
                case "Medium": medium++; break;
                default: weak++;
            }
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
        List<Credential> credentials = credentialRepository.findAll();

        int fresh = 0;
        int old = 0;
        int veryOld = 0;

        for (Credential cred : credentials) {
            long days = cred.getCreatedAt() != null
                    ? ChronoUnit.DAYS.between(cred.getCreatedAt(), LocalDateTime.now())
                    : 0;
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
            String password = decryptPasswordForCredential(cred, masterKey);
            passwordMap.computeIfAbsent(password, k -> new ArrayList<>()).add(cred);
        }

        // Filter only reused non-empty passwords
        return passwordMap.entrySet().stream()
                .filter(entry -> entry.getKey() != null && !entry.getKey().isEmpty() && entry.getValue().size() > 1)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Get top security recommendations with enhanced personal info detection
     */
    public List<SecurityRecommendation> getRecommendations(SecretKey masterKey) throws Exception {
        List<SecurityRecommendation> recommendations = new ArrayList<>();
        List<Credential> credentials = credentialRepository.findAll();

        int weakCount = 0;
        int personalInfoCount = 0;
        for (Credential cred : credentials) {
            String password = decryptPasswordForCredential(cred, masterKey);
            PasswordStrengthService.PersonalInfoContext context = buildPersonalInfoContext(cred);
            PasswordStrengthService.StrengthResult result = passwordStrengthService.calculateStrength(password, context);

            if (result.getScore() < 50) {
                weakCount++;
            }
            if (result.containsPersonalInfo()) {
                personalInfoCount++;
            }
        }

        if (personalInfoCount > 0) {
            recommendations.add(new SecurityRecommendation(
                    "🔴 CRITICAL",
                    "Personal Info in Passwords",
                    personalInfoCount + " passwords contain personal information (username, email, phone, or dates). This makes them easier to guess.",
                    "high"
            ));
        }

        if (weakCount > 0) {
            recommendations.add(new SecurityRecommendation(
                    "🔴 CRITICAL",
                    "Weak Passwords Detected",
                    weakCount + " passwords are weak and should be strengthened immediately.",
                    "high"
            ));
        }

        int oldCount = 0;
        for (Credential cred : credentials) {
            long days = cred.getCreatedAt() != null
                    ? ChronoUnit.DAYS.between(cred.getCreatedAt(), LocalDateTime.now())
                    : 0;
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

        Map<String, List<Credential>> reused = detectPasswordReuse(masterKey);
        if (!reused.isEmpty()) {
            recommendations.add(new SecurityRecommendation(
                    "🟠 IMPORTANT",
                    "Password Reuse Detected",
                    reused.size() + " passwords are being reused across multiple accounts.",
                    "high"
            ));
        }

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

        if (!credentials.isEmpty()) {
            int totalScore = 0;
            long totalAge = 0;

            for (Credential cred : credentials) {
                String password = decryptPasswordForCredential(cred, masterKey);
                totalScore += calculatePasswordStrengthScoreWithContext(password, cred);
                long age = cred.getCreatedAt() != null
                        ? ChronoUnit.DAYS.between(cred.getCreatedAt(), LocalDateTime.now())
                        : 0;
                totalAge += age;
            }

            stats.averagePasswordStrength = totalScore / credentials.size();
            stats.averagePasswordAge = (int) (totalAge / credentials.size());
        }

        Map<String, List<Credential>> reused = detectPasswordReuse(masterKey);
        stats.reusedPasswordCount = reused.values().stream()
                .mapToInt(List::size)
                .sum();

        return stats;
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