package com.passman.android.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.passman.android.PassManApp;
import com.passman.android.data.entity.UserEntity;
import com.passman.android.data.repository.UserRepository;
import com.passman.android.security.CryptoManager;
import com.passman.android.security.SessionManager;

import java.util.Arrays;

import javax.crypto.SecretKey;

/**
 * ViewModel for authentication (login/create vault)
 */
public class AuthViewModel extends AndroidViewModel {

    private final UserRepository userRepository;
    private final SessionManager sessionManager;

    private final MutableLiveData<AuthState> authState = new MutableLiveData<>(AuthState.LOADING);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> vaultExists = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        PassManApp app = (PassManApp) application;
        this.userRepository = app.getUserRepository();
        this.sessionManager = app.getSessionManager();
        
        checkVaultExists();
    }

    private void checkVaultExists() {
        authState.setValue(AuthState.LOADING);
        userRepository.vaultExists(new UserRepository.RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                vaultExists.postValue(exists);
                if (exists) {
                    authState.postValue(AuthState.LOGIN);
                } else {
                    authState.postValue(AuthState.CREATE_VAULT);
                }
            }

            @Override
            public void onError(Exception e) {
                errorMessage.postValue("Database error: " + e.getMessage());
                authState.postValue(AuthState.CREATE_VAULT);
            }
        });
    }

    /**
     * Attempt to login with master password
     */
    public void login(String password) {
        if (password == null || password.isEmpty()) {
            errorMessage.setValue("Please enter your master password");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        userRepository.getMasterUser(new UserRepository.RepositoryCallback<UserEntity>() {
            @Override
            public void onSuccess(UserEntity user) {
                if (user == null) {
                    isLoading.postValue(false);
                    errorMessage.postValue("Vault not found");
                    return;
                }

                char[] passwordChars = password.toCharArray();
                try {
                    boolean valid = CryptoManager.verifyPassword(
                            passwordChars,
                            user.getSalt(),
                            user.getHashedPassword()
                    );

                    if (valid) {
                        SecretKey masterKey = CryptoManager.deriveKey(passwordChars, user.getSalt());
                        sessionManager.initSession(user.getId(), masterKey);
                        
                        // Update last login
                        userRepository.updateLastLogin(user.getId(), null);
                        
                        authState.postValue(AuthState.SUCCESS);
                    } else {
                        errorMessage.postValue("Invalid master password");
                    }
                } catch (Exception e) {
                    errorMessage.postValue("Authentication error: " + e.getMessage());
                } finally {
                    Arrays.fill(passwordChars, '\0');
                    isLoading.postValue(false);
                }
            }

            @Override
            public void onError(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Login failed: " + e.getMessage());
            }
        });
    }

    /**
     * Create a new vault with master password
     */
    public void createVault(String password, String confirmPassword) {
        // Validation
        if (password == null || password.isEmpty()) {
            errorMessage.setValue("Please enter a master password");
            return;
        }

        if (password.length() < 8) {
            errorMessage.setValue("Password must be at least 8 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Passwords don't match");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        char[] passwordChars = password.toCharArray();
        try {
            byte[] salt = CryptoManager.generateSalt();
            byte[] hashedPassword = CryptoManager.hashPassword(passwordChars, salt);
            SecretKey masterKey = CryptoManager.deriveKey(passwordChars, salt);

            UserEntity user = new UserEntity();
            user.setSalt(salt);
            user.setHashedPassword(hashedPassword);

            userRepository.createMasterUser(user, new UserRepository.RepositoryCallback<Long>() {
                @Override
                public void onSuccess(Long userId) {
                    sessionManager.initSession(userId, masterKey);
                    authState.postValue(AuthState.SUCCESS);
                    isLoading.postValue(false);
                }

                @Override
                public void onError(Exception e) {
                    errorMessage.postValue("Failed to create vault: " + e.getMessage());
                    isLoading.postValue(false);
                }
            });
        } catch (Exception e) {
            errorMessage.postValue("Encryption error: " + e.getMessage());
            isLoading.postValue(false);
        } finally {
            Arrays.fill(passwordChars, '\0');
        }
    }

    /**
     * Authenticate with biometric and stored key
     */
    public void authenticateWithBiometric() {
        SecretKey storedKey = sessionManager.retrieveMasterKeyForBiometric();
        if (storedKey != null) {
            userRepository.getMasterUser(new UserRepository.RepositoryCallback<UserEntity>() {
                @Override
                public void onSuccess(UserEntity user) {
                    if (user != null) {
                        sessionManager.initSession(user.getId(), storedKey);
                        userRepository.updateLastLogin(user.getId(), null);
                        authState.postValue(AuthState.SUCCESS);
                    }
                }

                @Override
                public void onError(Exception e) {
                    errorMessage.postValue("Biometric auth failed");
                }
            });
        } else {
            errorMessage.setValue("Biometric key not found. Please login with password.");
        }
    }

    // ==================== GETTERS ====================

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Boolean> getVaultExists() {
        return vaultExists;
    }

    public boolean canUseBiometric() {
        PassManApp app = (PassManApp) getApplication();
        return app.getBiometricManager().isBiometricAvailable() 
                && sessionManager.hasBiometricKey();
    }

    // ==================== STATE ENUM ====================

    public enum AuthState {
        LOADING,
        LOGIN,
        CREATE_VAULT,
        SUCCESS
    }
}
