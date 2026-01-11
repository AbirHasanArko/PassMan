package com.passman.android.data.repository;

import androidx.lifecycle.LiveData;

import com.passman.android.data.dao.UserDao;
import com.passman.android.data.entity.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for user data operations
 */
public class UserRepository {

    private final UserDao userDao;
    private final ExecutorService executorService;

    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    // ==================== CREATE ====================

    public void createMasterUser(UserEntity user, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                // Set defaults
                user.setUsername("master");
                user.setCreatedAt(System.currentTimeMillis());
                user.setLastLogin(System.currentTimeMillis());
                user.setAutoLockTimeout(5); // 5 minutes default
                user.setClipboardTimeout(30); // 30 seconds default
                user.setThemeMode("system");
                
                long id = userDao.insert(user);
                if (callback != null) callback.onSuccess(id);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // ==================== READ ====================

    public void getMasterUser(RepositoryCallback<UserEntity> callback) {
        executorService.execute(() -> {
            try {
                UserEntity user = userDao.getMasterUser();
                if (callback != null) callback.onSuccess(user);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public LiveData<UserEntity> getMasterUserLive() {
        return userDao.getMasterUserLive();
    }

    public void vaultExists(RepositoryCallback<Boolean> callback) {
        executorService.execute(() -> {
            try {
                boolean exists = userDao.vaultExists();
                if (callback != null) callback.onSuccess(exists);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // ==================== UPDATE ====================

    public void updateLastLogin(long userId, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.updateLastLogin(userId, System.currentTimeMillis());
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void updatePassword(long userId, byte[] hashedPassword, byte[] salt, 
                               RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.updatePassword(userId, hashedPassword, salt);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void updateBiometricEnabled(long userId, boolean enabled, 
                                        RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.updateBiometricEnabled(userId, enabled);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void updateAutoLockTimeout(long userId, int timeout, 
                                       RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.updateAutoLockTimeout(userId, timeout);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void updateClipboardTimeout(long userId, int timeout, 
                                        RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.updateClipboardTimeout(userId, timeout);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void updateThemeMode(long userId, String themeMode, 
                                 RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                userDao.updateThemeMode(userId, themeMode);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }
}
