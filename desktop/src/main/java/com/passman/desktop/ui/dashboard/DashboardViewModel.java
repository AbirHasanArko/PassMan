package com.passman.desktop.ui.dashboard;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.Credential;
import com.passman.core.repository.CredentialRepository;
import com.passman.core.repository.CredentialRepositoryImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ViewModel for Dashboard with credential management
 */
public class DashboardViewModel {

    // Search Scope enum
    public enum SearchScope {
        ALL("All Fields"),
        TITLE("Title Only"),
        EMAIL("Email Only"),
        USERNAME("Username Only"),
        URL("URL Only"),
        TAGS("Tags Only");

        private final String displayName;
        SearchScope(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    // Filter Enums
    public enum AgeFilter {
        ALL("All Ages"),
        FRESH("Fresh (< 30 days)"),
        MODERATE("Moderate (30-90 days)"),
        OLD("Old (90-365 days)"),
        VERY_OLD("Very Old (> 365 days)");

        private final String displayName;
        AgeFilter(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum StrengthFilter {
        ALL("All Strengths"),
        WEAK("Weak"),
        MEDIUM("Medium"),
        STRONG("Strong");

        private final String displayName;
        StrengthFilter(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    public enum SortOption {
        TITLE_ASC("Title (A-Z)"),
        TITLE_DESC("Title (Z-A)"),
        RECENT("Recently Created"),
        OLDEST("Oldest First"),
        STRENGTH_ASC("Strength (Weak first)"),
        STRENGTH_DESC("Strength (Strong first)"),
        AGE_ASC("Age (Newest first)"),
        AGE_DESC("Age (Oldest first)");

        private final String displayName;
        SortOption(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final ObjectProperty<SearchScope> searchScope = new SimpleObjectProperty<>(SearchScope.ALL);
    private final ObjectProperty<AgeFilter> ageFilter = new SimpleObjectProperty<>(AgeFilter.ALL);
    private final ObjectProperty<StrengthFilter> strengthFilter = new SimpleObjectProperty<>(StrengthFilter.ALL);
    private final ObjectProperty<SortOption> sortOption = new SimpleObjectProperty<>(SortOption.TITLE_ASC);
    private final BooleanProperty favoritesOnly = new SimpleBooleanProperty(false);
    private final BooleanProperty breachedOnly = new SimpleBooleanProperty(false);

    private final ObservableList<CredentialItem> credentials = FXCollections.observableArrayList();
    private List<Credential> allCredentials;

    private final CredentialRepository credentialRepository;
    private SecretKey masterKey;

    public DashboardViewModel() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        this.credentialRepository = new CredentialRepositoryImpl(dbManager);

        searchQuery.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        searchScope.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        ageFilter.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        strengthFilter.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        sortOption.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        favoritesOnly.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        breachedOnly.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
    }

    public DashboardViewModel(CredentialRepository repository) {
        this.credentialRepository = repository;
        searchQuery.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        searchScope.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        ageFilter.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        strengthFilter.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        sortOption.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        favoritesOnly.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
        breachedOnly.addListener((obs, oldVal, newVal) -> applyFiltersAndSort());
    }

    public void setMasterKey(SecretKey key) {
        this.masterKey = key;
        loadCredentials();
    }

    public void loadCredentials() {
        try {
            allCredentials = credentialRepository.findAll();
            applyFiltersAndSort();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void applyFiltersAndSort() {
        if (allCredentials == null) return;

        List<Credential> filtered = allCredentials.stream()
                // Search filter based on scope
                .filter(cred -> {
                    String query = searchQuery.get();
                    if (query == null || query.trim().isEmpty()) return true;
                    String lowerQuery = query.toLowerCase();
                    SearchScope scope = searchScope.get();

                    if (scope == null || scope == SearchScope.ALL) {
                        return (cred.getTitle() != null && cred.getTitle().toLowerCase().contains(lowerQuery)) ||
                                (cred.getUsername() != null && cred.getUsername().toLowerCase().contains(lowerQuery)) ||
                                (cred.getEmail() != null && cred.getEmail().toLowerCase().contains(lowerQuery)) ||
                                (cred.getUrl() != null && cred.getUrl().toLowerCase().contains(lowerQuery)) ||
                                (cred.getTags() != null && cred.getTags().toLowerCase().contains(lowerQuery));
                    }

                    return switch (scope) {
                        case TITLE -> cred.getTitle() != null && cred.getTitle().toLowerCase().contains(lowerQuery);
                        case EMAIL -> cred.getEmail() != null && cred.getEmail().toLowerCase().contains(lowerQuery);
                        case USERNAME -> cred.getUsername() != null && cred.getUsername().toLowerCase().contains(lowerQuery);
                        case URL -> cred.getUrl() != null && cred.getUrl().toLowerCase().contains(lowerQuery);
                        case TAGS -> cred.getTags() != null && cred.getTags().toLowerCase().contains(lowerQuery);
                        default -> true;
                    };
                })
                // Age filter
                .filter(cred -> {
                    AgeFilter age = ageFilter.get();
                    if (age == null || age == AgeFilter.ALL) return true;
                    long days = ChronoUnit.DAYS.between(cred.getCreatedAt(), LocalDateTime.now());
                    return switch (age) {
                        case FRESH -> days < 30;
                        case MODERATE -> days >= 30 && days < 90;
                        case OLD -> days >= 90 && days < 365;
                        case VERY_OLD -> days >= 365;
                        default -> true;
                    };
                })
                // Strength filter
                .filter(cred -> {
                    StrengthFilter strength = strengthFilter.get();
                    if (strength == null || strength == StrengthFilter.ALL) return true;
                    int score = cred.getPasswordStrengthScore() != null ? cred.getPasswordStrengthScore() : 0;
                    return switch (strength) {
                        case WEAK -> score < 50;
                        case MEDIUM -> score >= 50 && score < 75;
                        case STRONG -> score >= 75;
                        default -> true;
                    };
                })
                // Favorites filter
                .filter(cred -> !favoritesOnly.get() || cred.isFavorite())
                // Breached filter
                .filter(cred -> !breachedOnly.get() || cred.isBreached())
                .collect(Collectors.toList());

        // Apply sorting
        SortOption sort = sortOption.get();
        if (sort != null) {
            Comparator<Credential> comparator = switch (sort) {
                case TITLE_ASC -> Comparator.comparing(Credential::getTitle, String.CASE_INSENSITIVE_ORDER);
                case TITLE_DESC -> Comparator.comparing(Credential::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
                case RECENT -> Comparator.comparing(Credential::getCreatedAt).reversed();
                case OLDEST -> Comparator.comparing(Credential::getCreatedAt);
                case STRENGTH_ASC -> Comparator.comparing(c -> c.getPasswordStrengthScore() != null ? c.getPasswordStrengthScore() : 0);
                case STRENGTH_DESC -> Comparator.comparing((Credential c) -> c.getPasswordStrengthScore() != null ? c.getPasswordStrengthScore() : 0).reversed();
                case AGE_ASC -> Comparator.comparing(Credential::getCreatedAt).reversed();
                case AGE_DESC -> Comparator.comparing(Credential::getCreatedAt);
            };
            filtered.sort(comparator);
        }

        credentials.clear();
        for (Credential cred : filtered) {
            credentials.add(mapToCredentialItem(cred));
        }
    }

    private void performSearch(String query) {
        applyFiltersAndSort();
    }

    private CredentialItem mapToCredentialItem(Credential cred) {
        CredentialItem item = new CredentialItem();
        item.setId(cred.getId());
        item.setTitle(cred.getTitle());
        item.setUsername(cred.getUsername());
        item.setEmail(cred.getEmail());
        item.setUrl(cred.getUrl());
        item.setAgeBadge(calculateAgeBadge(cred.getCreatedAt()));
        item.setStrength(calculateStrength(cred));
        item.setHasReuse(false);
        return item;
    }

    private String calculateAgeBadge(LocalDateTime createdAt) {
        long days = ChronoUnit.DAYS.between(createdAt, LocalDateTime.now());
        if (days < 90) return "Fresh";
        if (days < 365) return "Old";
        return "Very Old";
    }

    private String calculateStrength(Credential cred) {
        int score = cred.getPasswordStrengthScore();
        if (score >= 75) return "Strong";
        else if (score >= 50) return "Medium";
        else return "Weak";
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    public ObjectProperty<SearchScope> searchScopeProperty() {
        return searchScope;
    }

    public ObjectProperty<AgeFilter> ageFilterProperty() {
        return ageFilter;
    }

    public ObjectProperty<StrengthFilter> strengthFilterProperty() {
        return strengthFilter;
    }

    public ObjectProperty<SortOption> sortOptionProperty() {
        return sortOption;
    }

    public BooleanProperty favoritesOnlyProperty() {
        return favoritesOnly;
    }

    public BooleanProperty breachedOnlyProperty() {
        return breachedOnly;
    }

    public void clearFilters() {
        searchScope.set(SearchScope.ALL);
        ageFilter.set(AgeFilter.ALL);
        strengthFilter.set(StrengthFilter.ALL);
        sortOption.set(SortOption.TITLE_ASC);
        favoritesOnly.set(false);
        breachedOnly.set(false);
        searchQuery.set("");
    }

    public ObservableList<CredentialItem> getCredentials() {
        return credentials;
    }

    /**
     * UI-friendly credential item
     */
    public static class CredentialItem {
        private Long id;
        private String title;
        private String username;
        private String email;
        private String url;
        private String ageBadge;
        private String strength;
        private boolean hasReuse;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }

        public String getAgeBadge() { return ageBadge; }
        public void setAgeBadge(String ageBadge) { this.ageBadge = ageBadge; }

        public String getStrength() { return strength; }
        public void setStrength(String strength) { this.strength = strength; }

        public boolean getHasReuse() { return hasReuse; }
        public void setHasReuse(boolean hasReuse) { this.hasReuse = hasReuse; }
    }
}