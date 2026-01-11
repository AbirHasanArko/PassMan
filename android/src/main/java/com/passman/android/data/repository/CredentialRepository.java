package com.passman.android.data.repository;

import androidx.lifecycle.LiveData;

import com.passman.android.data.dao.CredentialDao;
import com.passman.android.data.entity.CredentialEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for credential data operations
 */
public class CredentialRepository {

    private final CredentialDao credentialDao;
    private final ExecutorService executorService;

    public CredentialRepository(CredentialDao credentialDao) {
        this.credentialDao = credentialDao;
        this.executorService = Executors.newFixedThreadPool(4);
    }

    // ==================== INSERT ====================

    public void insert(CredentialEntity credential, RepositoryCallback<Long> callback) {
        executorService.execute(() -> {
            try {
                long id = credentialDao.insert(credential);
                if (callback != null) callback.onSuccess(id);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // ==================== UPDATE ====================

    public void update(CredentialEntity credential, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                credentialDao.update(credential);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // ==================== DELETE ====================

    public void delete(CredentialEntity credential, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                credentialDao.delete(credential);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void deleteById(long id, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                credentialDao.deleteById(id);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public void deleteAll(RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                credentialDao.deleteAll();
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // ==================== QUERY ====================

    public void getById(long id, RepositoryCallback<CredentialEntity> callback) {
        executorService.execute(() -> {
            try {
                CredentialEntity credential = credentialDao.getById(id);
                if (callback != null) callback.onSuccess(credential);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public LiveData<CredentialEntity> getByIdLive(long id) {
        return credentialDao.getByIdLive(id);
    }

    public LiveData<List<CredentialEntity>> getAllLive() {
        return credentialDao.getAllLive();
    }

    public void getAll(RepositoryCallback<List<CredentialEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<CredentialEntity> credentials = credentialDao.getAll();
                if (callback != null) callback.onSuccess(credentials);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public LiveData<List<CredentialEntity>> getFavoritesLive() {
        return credentialDao.getFavoritesLive();
    }

    public LiveData<List<CredentialEntity>> searchLive(String query) {
        return credentialDao.searchLive(query);
    }

    public void search(String query, RepositoryCallback<List<CredentialEntity>> callback) {
        executorService.execute(() -> {
            try {
                List<CredentialEntity> results = credentialDao.search(query);
                if (callback != null) callback.onSuccess(results);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    public LiveData<List<CredentialEntity>> getByCategoryLive(String category) {
        return credentialDao.getByCategoryLive(category);
    }

    // ==================== STATISTICS ====================

    public LiveData<Integer> getCountLive() {
        return credentialDao.getCountLive();
    }

    public LiveData<Integer> getWeakPasswordCountLive() {
        return credentialDao.getWeakPasswordCountLive();
    }

    public LiveData<Integer> getAverageStrengthScoreLive() {
        return credentialDao.getAverageStrengthScoreLive();
    }

    public void getStatistics(RepositoryCallback<CredentialStatistics> callback) {
        executorService.execute(() -> {
            try {
                int total = credentialDao.getCount();
                int weak = credentialDao.getWeakPasswordCount();
                int old = credentialDao.getOldPasswordCount(System.currentTimeMillis());
                int avgScore = credentialDao.getAverageStrengthScore();
                
                CredentialStatistics stats = new CredentialStatistics(total, weak, old, avgScore);
                if (callback != null) callback.onSuccess(stats);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // ==================== FAVORITES ====================

    public void toggleFavorite(long id, boolean isFavorite, RepositoryCallback<Void> callback) {
        executorService.execute(() -> {
            try {
                credentialDao.updateFavorite(id, isFavorite);
                if (callback != null) callback.onSuccess(null);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // ==================== CATEGORIES ====================

    public void getAllCategories(RepositoryCallback<List<String>> callback) {
        executorService.execute(() -> {
            try {
                List<String> categories = credentialDao.getAllCategories();
                if (callback != null) callback.onSuccess(categories);
            } catch (Exception e) {
                if (callback != null) callback.onError(e);
            }
        });
    }

    // ==================== HELPER CLASSES ====================

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(Exception e);
    }

    public static class CredentialStatistics {
        public final int totalCount;
        public final int weakCount;
        public final int oldCount;
        public final int averageScore;

        public CredentialStatistics(int totalCount, int weakCount, int oldCount, int averageScore) {
            this.totalCount = totalCount;
            this.weakCount = weakCount;
            this.oldCount = oldCount;
            this.averageScore = averageScore;
        }

        public int getSecurityScore() {
            if (totalCount == 0) return 100;
            int issues = weakCount + oldCount;
            double ratio = (double) issues / totalCount;
            return Math.max(0, Math.min(100, (int) ((1 - ratio) * 100)));
        }
    }
}
