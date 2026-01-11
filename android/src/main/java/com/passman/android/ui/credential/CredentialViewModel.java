package com.passman.android.ui.credential;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.passman.android.PassManApp;
import com.passman.android.data.entity.CredentialEntity;
import com.passman.android.data.repository.CredentialRepository;
import com.passman.android.security.CryptoManager;
import com.passman.android.security.PasswordStrengthService;
import com.passman.android.security.SessionManager;

import javax.crypto.SecretKey;

/**
 * ViewModel for adding/editing credentials
 */
public class CredentialViewModel extends AndroidViewModel {

    private final CredentialRepository credentialRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<CredentialEntity> credential = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> decryptedPassword = new MutableLiveData<>();
    private final MutableLiveData<Integer> passwordStrength = new MutableLiveData<>(0);
    private final MutableLiveData<String> passwordWarning = new MutableLiveData<>();
    private final MutableLiveData<Boolean> containsPersonalInfo = new MutableLiveData<>(false);

    // Personal info context for strength checking
    private String currentUsername = "";
    private String currentEmail = "";

    private boolean isEditMode = false;
    private long credentialId = -1;

    public CredentialViewModel(@NonNull Application application) {
        super(application);
        PassManApp app = (PassManApp) application;
        this.credentialRepository = app.getCredentialRepository();
        this.sessionManager = app.getSessionManager();
    }

    /**
     * Load an existing credential for editing
     */
    public void loadCredential(long id) {
        if (id <= 0) return;
        
        isEditMode = true;
        credentialId = id;
        isLoading.setValue(true);

        credentialRepository.getById(id, new CredentialRepository.RepositoryCallback<CredentialEntity>() {
            @Override
            public void onSuccess(CredentialEntity result) {
                credential.postValue(result);
                
                // Decrypt password
                if (result != null && result.getEncryptedPassword() != null) {
                    try {
                        SecretKey key = sessionManager.getMasterKey();
                        byte[] decrypted = CryptoManager.decryptBytes(
                                result.getEncryptedPassword(), key);
                        String password = new String(decrypted);
                        decryptedPassword.postValue(password);
                        passwordStrength.postValue(CryptoManager.calculatePasswordStrength(password));
                    } catch (Exception e) {
                        errorMessage.postValue("Failed to decrypt password");
                    }
                }
                
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Failed to load credential: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Save the credential (create or update)
     */
    public void saveCredential(String title, String username, String email, 
                               String url, String password, String notes, 
                               String tags, String category) {
        // Validation
        if (title == null || title.trim().isEmpty()) {
            errorMessage.setValue("Title is required");
            return;
        }

        if (password == null || password.isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        isLoading.setValue(true);

        try {
            SecretKey key = sessionManager.getMasterKey();
            if (key == null) {
                errorMessage.setValue("Session expired. Please login again.");
                isLoading.setValue(false);
                return;
            }

            // Encrypt password
            byte[] encryptedPassword = CryptoManager.encryptBytes(password.getBytes(), key);
            int strength = CryptoManager.calculatePasswordStrength(password);

            CredentialEntity entity;
            if (isEditMode && credential.getValue() != null) {
                entity = credential.getValue();
            } else {
                entity = new CredentialEntity();
                entity.setCreatedAt(System.currentTimeMillis());
            }

            entity.setTitle(title.trim());
            entity.setUsername(username != null ? username.trim() : "");
            entity.setEmail(email != null ? email.trim() : "");
            entity.setUrl(url != null ? url.trim() : "");
            entity.setEncryptedPassword(encryptedPassword);
            entity.setNotes(notes != null ? notes.trim() : "");
            entity.setTags(tags != null ? tags.trim() : "");
            entity.setCategory(category);
            entity.setLastModified(System.currentTimeMillis());
            entity.setPasswordChangedAt(System.currentTimeMillis());
            entity.setPasswordStrengthScore(strength);

            if (isEditMode) {
                credentialRepository.update(entity, new CredentialRepository.RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        saveSuccess.postValue(true);
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.postValue("Failed to update: " + e.getMessage());
                        isLoading.postValue(false);
                    }
                });
            } else {
                credentialRepository.insert(entity, new CredentialRepository.RepositoryCallback<Long>() {
                    @Override
                    public void onSuccess(Long id) {
                        saveSuccess.postValue(true);
                        isLoading.postValue(false);
                    }

                    @Override
                    public void onError(Exception e) {
                        errorMessage.postValue("Failed to save: " + e.getMessage());
                        isLoading.postValue(false);
                    }
                });
            }
        } catch (Exception e) {
            errorMessage.setValue("Encryption error: " + e.getMessage());
            isLoading.setValue(false);
        }
    }

    /**
     * Delete the current credential
     */
    public void deleteCredential() {
        if (!isEditMode || credential.getValue() == null) return;

        isLoading.setValue(true);
        credentialRepository.delete(credential.getValue(), 
                new CredentialRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                saveSuccess.postValue(true);
                isLoading.postValue(false);
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Failed to delete: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    /**
     * Toggle favorite status
     */
    public void toggleFavorite() {
        if (credential.getValue() == null) return;
        
        CredentialEntity entity = credential.getValue();
        entity.setFavorite(!entity.isFavorite());
        
        credentialRepository.toggleFavorite(entity.getId(), entity.isFavorite(), null);
        credential.setValue(entity);
    }

    /**
     * Update password strength when password changes
     */
    public void updatePasswordStrength(String password) {
        PasswordStrengthService.PersonalInfoContext context = buildPersonalInfoContext();
        PasswordStrengthService.StrengthResult result = CryptoManager.getPasswordStrengthResult(password, context);
        
        passwordStrength.setValue(result.getScore());
        passwordWarning.setValue(result.getWarningMessage());
        containsPersonalInfo.setValue(result.containsPersonalInfo());
    }

    /**
     * Update username for personal info checking
     */
    public void setCurrentUsername(String username) {
        this.currentUsername = username != null ? username : "";
        // Re-evaluate password strength if password exists
        String password = decryptedPassword.getValue();
        if (password != null && !password.isEmpty()) {
            updatePasswordStrength(password);
        }
    }

    /**
     * Update email for personal info checking
     */
    public void setCurrentEmail(String email) {
        this.currentEmail = email != null ? email : "";
        // Re-evaluate password strength if password exists
        String password = decryptedPassword.getValue();
        if (password != null && !password.isEmpty()) {
            updatePasswordStrength(password);
        }
    }

    /**
     * Build personal info context for strength checking
     */
    private PasswordStrengthService.PersonalInfoContext buildPersonalInfoContext() {
        PasswordStrengthService.PersonalInfoContext context = new PasswordStrengthService.PersonalInfoContext();
        context.setUsername(currentUsername);
        context.setEmail(currentEmail);
        return context;
    }

    /**
     * Generate a new password
     */
    public String generatePassword(int length, boolean uppercase, boolean lowercase,
                                   boolean numbers, boolean symbols, boolean excludeAmbiguous) {
        String password = CryptoManager.generatePassword(
                length, uppercase, lowercase, numbers, symbols, excludeAmbiguous);
        decryptedPassword.setValue(password);
        updatePasswordStrength(password);
        return password;
    }

    // ==================== GETTERS ====================

    public LiveData<CredentialEntity> getCredential() {
        return credential;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }

    public LiveData<String> getDecryptedPassword() {
        return decryptedPassword;
    }

    public LiveData<Integer> getPasswordStrength() {
        return passwordStrength;
    }

    public LiveData<String> getPasswordWarning() {
        return passwordWarning;
    }

    public LiveData<Boolean> getContainsPersonalInfo() {
        return containsPersonalInfo;
    }

    public boolean isEditMode() {
        return isEditMode;
    }
}
