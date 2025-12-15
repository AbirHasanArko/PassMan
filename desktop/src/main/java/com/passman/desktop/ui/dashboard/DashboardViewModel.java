package com.passman.desktop.ui.dashboard;

import com.passman.core.db.DatabaseManager;
import com. passman.core.model.Credential;
import com. passman.core.repository.CredentialRepository;
import com.passman.core.repository.CredentialRepositoryImpl;
import javafx.beans.property.SimpleStringProperty;
import javafx. beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx. collections.ObservableList;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java. time.temporal.ChronoUnit;
import java.util.List;

/**
 * ViewModel for Dashboard with credential management
 */
public class DashboardViewModel {

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final ObservableList<CredentialItem> credentials = FXCollections.observableArrayList();

    private final CredentialRepository credentialRepository;
    private SecretKey masterKey;

    public DashboardViewModel() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        this.credentialRepository = new CredentialRepositoryImpl(dbManager);

        searchQuery.addListener((obs, oldVal, newVal) -> performSearch(newVal));
    }

    public DashboardViewModel(CredentialRepository repository) {
        this.credentialRepository = repository;
        searchQuery.addListener((obs, oldVal, newVal) -> performSearch(newVal));
    }

    public void setMasterKey(SecretKey key) {
        this.masterKey = key;
        loadCredentials();
    }

    public void loadCredentials() {
        try {
            credentials.clear();
            List<Credential> allCreds = credentialRepository.findAll();

            for (Credential cred : allCreds) {
                credentials.add(mapToCredentialItem(cred));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void performSearch(String query) {
        try {
            credentials.clear();

            if (query == null || query.trim().isEmpty()) {
                loadCredentials();
                return;
            }

            List<Credential> results = credentialRepository.searchByTitle(query);
            for (Credential cred : results) {
                credentials.add(mapToCredentialItem(cred));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private CredentialItem mapToCredentialItem(Credential cred) {
        CredentialItem item = new CredentialItem();
        item.setId(cred.getId());
        item.setTitle(cred.getTitle());
        item.setUsername(cred.getUsername());
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
        int length = cred.getEncryptedPassword().length;
        if (length > 50) return "Strong";
        if (length > 30) return "Medium";
        return "Weak";
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
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