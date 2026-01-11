package com.passman.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.passman.android.data.entity.CredentialEntity;

import java.util.List;

/**
 * Data Access Object for Credential operations
 */
@Dao
public interface CredentialDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CredentialEntity credential);

    @Update
    void update(CredentialEntity credential);

    @Delete
    void delete(CredentialEntity credential);

    @Query("DELETE FROM credentials WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM credentials WHERE id = :id")
    CredentialEntity getById(long id);

    @Query("SELECT * FROM credentials WHERE id = :id")
    LiveData<CredentialEntity> getByIdLive(long id);

    @Query("SELECT * FROM credentials ORDER BY last_modified DESC")
    LiveData<List<CredentialEntity>> getAllLive();

    @Query("SELECT * FROM credentials ORDER BY last_modified DESC")
    List<CredentialEntity> getAll();

    @Query("SELECT * FROM credentials WHERE is_favorite = 1 ORDER BY title ASC")
    LiveData<List<CredentialEntity>> getFavoritesLive();

    @Query("SELECT * FROM credentials WHERE is_favorite = 1 ORDER BY title ASC")
    List<CredentialEntity> getFavorites();

    @Query("SELECT * FROM credentials WHERE title LIKE '%' || :query || '%' " +
           "OR username LIKE '%' || :query || '%' " +
           "OR email LIKE '%' || :query || '%' " +
           "OR url LIKE '%' || :query || '%' " +
           "OR tags LIKE '%' || :query || '%' " +
           "ORDER BY title ASC")
    LiveData<List<CredentialEntity>> searchLive(String query);

    @Query("SELECT * FROM credentials WHERE title LIKE '%' || :query || '%' " +
           "OR username LIKE '%' || :query || '%' " +
           "OR email LIKE '%' || :query || '%' " +
           "OR url LIKE '%' || :query || '%' " +
           "OR tags LIKE '%' || :query || '%' " +
           "ORDER BY title ASC")
    List<CredentialEntity> search(String query);

    @Query("SELECT * FROM credentials WHERE category = :category ORDER BY title ASC")
    LiveData<List<CredentialEntity>> getByCategoryLive(String category);

    @Query("SELECT COUNT(*) FROM credentials")
    int getCount();

    @Query("SELECT COUNT(*) FROM credentials")
    LiveData<Integer> getCountLive();

    @Query("SELECT COUNT(*) FROM credentials WHERE password_strength_score < 50")
    int getWeakPasswordCount();

    @Query("SELECT COUNT(*) FROM credentials WHERE password_strength_score < 50")
    LiveData<Integer> getWeakPasswordCountLive();

    @Query("SELECT COUNT(*) FROM credentials WHERE " +
           "((:currentTime - password_changed_at) / 86400000) > 90")
    int getOldPasswordCount(long currentTime);

    @Query("SELECT AVG(password_strength_score) FROM credentials")
    int getAverageStrengthScore();

    @Query("SELECT AVG(password_strength_score) FROM credentials")
    LiveData<Integer> getAverageStrengthScoreLive();

    @Query("UPDATE credentials SET is_favorite = :isFavorite WHERE id = :id")
    void updateFavorite(long id, boolean isFavorite);

    @Query("UPDATE credentials SET password_strength_score = :score WHERE id = :id")
    void updateStrengthScore(long id, int score);

    @Query("UPDATE credentials SET is_breached = :isBreached WHERE id = :id")
    void updateBreached(long id, boolean isBreached);

    @Query("SELECT DISTINCT category FROM credentials WHERE category IS NOT NULL")
    List<String> getAllCategories();

    @Query("SELECT DISTINCT tags FROM credentials WHERE tags IS NOT NULL AND tags != ''")
    List<String> getAllTags();

    @Query("DELETE FROM credentials")
    void deleteAll();
}
