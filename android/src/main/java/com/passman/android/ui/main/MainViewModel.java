package com.passman.android.ui.main;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.passman.android.PassManApp;
import com.passman.android.data.entity.CredentialEntity;
import com.passman.android.data.repository.CredentialRepository;
import com.passman.android.security.CryptoManager;
import com.passman.android.security.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.crypto.SecretKey;

/**
 * ViewModel for the main dashboard
 */
public class MainViewModel extends AndroidViewModel {

    private final CredentialRepository credentialRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<FilterType> filterType = new MutableLiveData<>(FilterType.ALL);
    private final MutableLiveData<FilterSortBottomSheet.FilterSortOptions> filterSortOptions = 
            new MutableLiveData<>(FilterSortBottomSheet.FilterSortOptions.getDefault());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final LiveData<List<CredentialEntity>> allCredentials;
    private final LiveData<List<CredentialEntity>> favoriteCredentials;
    private final MediatorLiveData<List<CredentialEntity>> filteredCredentials;

    // Statistics
    private final LiveData<Integer> totalCount;
    private final LiveData<Integer> weakPasswordCount;
    private final LiveData<Integer> averageStrength;

    public MainViewModel(@NonNull Application application) {
        super(application);
        PassManApp app = (PassManApp) application;
        this.credentialRepository = app.getCredentialRepository();
        this.sessionManager = app.getSessionManager();

        // Initialize LiveData sources
        allCredentials = credentialRepository.getAllLive();
        favoriteCredentials = credentialRepository.getFavoritesLive();
        
        // Statistics
        totalCount = credentialRepository.getCountLive();
        weakPasswordCount = credentialRepository.getWeakPasswordCountLive();
        averageStrength = credentialRepository.getAverageStrengthScoreLive();

        // Setup filtered credentials
        filteredCredentials = new MediatorLiveData<>();
        setupFilter();
    }

    private void setupFilter() {
        // Add all sources. When ANY source changes, update the filtered list.
        filteredCredentials.addSource(allCredentials, list -> updateFilteredList());
        filteredCredentials.addSource(favoriteCredentials, list -> updateFilteredList());
        filteredCredentials.addSource(searchQuery, query -> updateFilteredList());
        filteredCredentials.addSource(filterType, type -> updateFilteredList());
        filteredCredentials.addSource(filterSortOptions, options -> updateFilteredList());
    }

    private void updateFilteredList() {
        List<CredentialEntity> all = allCredentials.getValue();
        List<CredentialEntity> favorites = favoriteCredentials.getValue();
        String query = searchQuery.getValue();
        FilterType type = filterType.getValue();
        FilterSortBottomSheet.FilterSortOptions options = filterSortOptions.getValue();

        if (all == null) {
            all = new ArrayList<>();
        }
        if (favorites == null) {
            favorites = new ArrayList<>();
        }
        if (query == null) {
            query = "";
        }
        if (type == null) {
            type = FilterType.ALL;
        }
        if (options == null) {
            options = FilterSortBottomSheet.FilterSortOptions.getDefault();
        }

        // Select source based on tab
        List<CredentialEntity> source = (type == FilterType.FAVORITES) ? favorites : all;

        // Apply search filter
        String searchTerm = query.trim().toLowerCase();
        List<CredentialEntity> filtered = new ArrayList<>();
        
        for (CredentialEntity cred : source) {
            if (!searchTerm.isEmpty() && !matchesQuery(cred, searchTerm)) {
                continue;
            }
            
            // Apply category filter
            if (options.getCategory() != null && 
                (cred.getCategory() == null || !cred.getCategory().equals(options.getCategory()))) {
                continue;
            }
            
            // Apply strength filter
            if (!matchesStrengthFilter(cred, options.getStrengthFilter())) {
                continue;
            }
            
            // Apply old passwords filter
            if (options.isFilterOldPasswords() && !isOldPassword(cred)) {
                continue;
            }
            
            // Apply breached filter
            if (options.isFilterBreached() && !cred.isBreached()) {
                continue;
            }
            
            filtered.add(cred);
        }
        
        // Apply sorting
        sortCredentials(filtered, options.getSortOption());
        
        filteredCredentials.setValue(filtered);
    }
    
    private boolean matchesStrengthFilter(CredentialEntity cred, FilterSortBottomSheet.StrengthFilter filter) {
        int score = cred.getPasswordStrengthScore();
        switch (filter) {
            case WEAK:
                return score < 30;
            case FAIR:
                return score >= 30 && score < 50;
            case GOOD:
                return score >= 50 && score < 70;
            case STRONG:
                return score >= 70;
            case ALL:
            default:
                return true;
        }
    }
    
    private boolean isOldPassword(CredentialEntity cred) {
        long passwordAge = System.currentTimeMillis() - cred.getPasswordChangedAt();
        long daysOld = passwordAge / (24 * 60 * 60 * 1000);
        return daysOld > 90;
    }
    
    private void sortCredentials(List<CredentialEntity> list, FilterSortBottomSheet.SortOption sortOption) {
        Comparator<CredentialEntity> comparator;
        
        switch (sortOption) {
            case A_TO_Z:
                comparator = (a, b) -> {
                    String titleA = a.getTitle() != null ? a.getTitle() : "";
                    String titleB = b.getTitle() != null ? b.getTitle() : "";
                    return titleA.compareToIgnoreCase(titleB);
                };
                break;
            case Z_TO_A:
                comparator = (a, b) -> {
                    String titleA = a.getTitle() != null ? a.getTitle() : "";
                    String titleB = b.getTitle() != null ? b.getTitle() : "";
                    return titleB.compareToIgnoreCase(titleA);
                };
                break;
            case OLDEST:
                comparator = Comparator.comparingLong(CredentialEntity::getLastModified);
                break;
            case STRENGTH:
                comparator = Comparator.comparingInt(CredentialEntity::getPasswordStrengthScore);
                break;
            case RECENT:
            default:
                comparator = (a, b) -> Long.compare(b.getLastModified(), a.getLastModified());
                break;
        }
        
        Collections.sort(list, comparator);
    }

    private boolean matchesQuery(CredentialEntity credential, String query) {
        return (credential.getTitle() != null && 
                credential.getTitle().toLowerCase().contains(query)) ||
               (credential.getUsername() != null && 
                credential.getUsername().toLowerCase().contains(query)) ||
               (credential.getEmail() != null && 
                credential.getEmail().toLowerCase().contains(query)) ||
               (credential.getUrl() != null && 
                credential.getUrl().toLowerCase().contains(query)) ||
               (credential.getTags() != null && 
                credential.getTags().toLowerCase().contains(query));
    }

    /**
     * Delete a credential
     */
    public void deleteCredential(CredentialEntity credential) {
        credentialRepository.delete(credential, new CredentialRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Handled by LiveData
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Failed to delete: " + e.getMessage());
            }
        });
    }

    /**
     * Toggle favorite status
     */
    public void toggleFavorite(CredentialEntity credential) {
        credentialRepository.toggleFavorite(
                credential.getId(),
                !credential.isFavorite(),
                new CredentialRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Handled by LiveData
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.postValue("Failed to update favorite");
                    }
                }
        );
    }

    /**
     * Get decrypted password for a credential
     */
    public String decryptPassword(CredentialEntity credential) {
        try {
            SecretKey key = sessionManager.getMasterKey();
            if (key == null) return null;

            byte[] decrypted = CryptoManager.decryptBytes(
                    credential.getEncryptedPassword(), key);
            return new String(decrypted);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Lock the vault
     */
    public void lockVault() {
        sessionManager.lockVault();
    }

    // ==================== SETTERS ====================

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public void setFilterType(FilterType type) {
        filterType.setValue(type);
    }
    
    public void setFilterSortOptions(FilterSortBottomSheet.FilterSortOptions options) {
        filterSortOptions.setValue(options);
    }

    // ==================== GETTERS ====================

    public LiveData<List<CredentialEntity>> getFilteredCredentials() {
        return filteredCredentials;
    }

    public LiveData<Integer> getTotalCount() {
        return totalCount;
    }

    public LiveData<Integer> getWeakPasswordCount() {
        return weakPasswordCount;
    }

    public LiveData<Integer> getAverageStrength() {
        return averageStrength;
    }

    public LiveData<FilterType> getFilterType() {
        return filterType;
    }
    
    public FilterSortBottomSheet.FilterSortOptions getCurrentFilterSortOptions() {
        FilterSortBottomSheet.FilterSortOptions options = filterSortOptions.getValue();
        return options != null ? options : FilterSortBottomSheet.FilterSortOptions.getDefault();
    }
    
    public LiveData<FilterSortBottomSheet.FilterSortOptions> getFilterSortOptions() {
        return filterSortOptions;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public int getSecurityScore() {
        Integer avg = averageStrength.getValue();
        return avg != null ? avg : 0;
    }

    // ==================== ENUMS ====================

    public enum FilterType {
        ALL,
        FAVORITES
    }
}
