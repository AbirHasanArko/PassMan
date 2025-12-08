package com.passman.desktop.ui.dashboard;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;

public class DashboardViewModel {

    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final ObservableList<CredentialItem> credentials = FXCollections.observableArrayList();
    private final FilteredList<CredentialItem> filteredCredentials;
    private final SortedList<CredentialItem> sortedCredentials;

    public DashboardViewModel() {
        loadMockData();

        filteredCredentials = new FilteredList<>(credentials, p -> true);

        searchQuery.addListener((obs, oldVal, newVal) -> {
            filteredCredentials.setPredicate(credential -> {
                if (newVal == null || newVal. isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newVal.toLowerCase();

                if (credential.getTitle().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (credential.getUsername(). toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (credential.getUrl().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }

                return false;
            });
        });

        sortedCredentials = new SortedList<>(filteredCredentials);
    }

    private void loadMockData() {
        credentials.addAll(
                new CredentialItem(1L, "GitHub", "john.doe@email.com", "https://github.com", 30, "Strong", false),
                new CredentialItem(2L, "Google", "john.doe@gmail.com", "https://google.com", 90, "Medium", false),
                new CredentialItem(3L, "Facebook", "johndoe", "https://facebook.com", 180, "Weak", true),
                new CredentialItem(4L, "Twitter", "@johndoe", "https://twitter.com", 45, "Strong", false),
                new CredentialItem(5L, "LinkedIn", "john.doe@email.com", "https://linkedin.com", 200, "Medium", true)
        );
    }

    public StringProperty searchQueryProperty() { return searchQuery; }
    public SortedList<CredentialItem> getSortedCredentials() { return sortedCredentials; }

    public static class CredentialItem {
        private final Long id;
        private final String title;
        private final String username;
        private final String url;
        private final int ageDays;
        private final String strength;
        private final boolean hasReuse;

        public CredentialItem(Long id, String title, String username, String url,
                              int ageDays, String strength, boolean hasReuse) {
            this.id = id;
            this.title = title;
            this.username = username;
            this.url = url;
            this.ageDays = ageDays;
            this.strength = strength;
            this.hasReuse = hasReuse;
        }

        public Long getId() { return id; }
        public String getTitle() { return title; }
        public String getUsername() { return username; }
        public String getUrl() { return url; }
        public int getAgeDays() { return ageDays; }
        public String getStrength() { return strength; }
        public boolean hasReuse() { return hasReuse; }

        public String getAgeBadge() {
            if (ageDays < 90) return "Fresh";
            if (ageDays < 180) return "Old";
            return "Very Old";
        }
    }
}