package com.passman.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.passman.android.data.entity.UserEntity;

/**
 * Data Access Object for User operations
 */
@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getByUsername(String username);

    @Query("SELECT * FROM users WHERE username = 'master' LIMIT 1")
    UserEntity getMasterUser();

    @Query("SELECT * FROM users WHERE username = 'master' LIMIT 1")
    LiveData<UserEntity> getMasterUserLive();

    @Query("SELECT COUNT(*) FROM users WHERE username = 'master'")
    int masterUserExists();

    @Query("SELECT COUNT(*) > 0 FROM users WHERE username = 'master'")
    boolean vaultExists();

    @Query("UPDATE users SET last_login = :timestamp WHERE id = :userId")
    void updateLastLogin(long userId, long timestamp);

    @Query("UPDATE users SET hashed_password = :hashedPassword, salt = :salt WHERE id = :userId")
    void updatePassword(long userId, byte[] hashedPassword, byte[] salt);

    @Query("UPDATE users SET biometric_enabled = :enabled WHERE id = :userId")
    void updateBiometricEnabled(long userId, boolean enabled);

    @Query("UPDATE users SET auto_lock_timeout = :timeout WHERE id = :userId")
    void updateAutoLockTimeout(long userId, int timeout);

    @Query("UPDATE users SET clipboard_timeout = :timeout WHERE id = :userId")
    void updateClipboardTimeout(long userId, int timeout);

    @Query("UPDATE users SET theme_mode = :themeMode WHERE id = :userId")
    void updateThemeMode(long userId, String themeMode);

    @Query("DELETE FROM users")
    void deleteAll();
}
