package com.passman.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.passman.android.data.entity.FileVaultEntity;

import java.util.List;

/**
 * Data Access Object for file vaults
 */
@Dao
public interface FileVaultDao {

    @Insert
    long insert(FileVaultEntity vault);

    @Update
    void update(FileVaultEntity vault);

    @Delete
    void delete(FileVaultEntity vault);

    @Query("DELETE FROM file_vaults WHERE id = :vaultId")
    void deleteById(long vaultId);

    @Query("SELECT * FROM file_vaults ORDER BY last_accessed DESC")
    LiveData<List<FileVaultEntity>> getAllVaults();

    @Query("SELECT * FROM file_vaults ORDER BY last_accessed DESC")
    List<FileVaultEntity> getAllVaultsSync();

    @Query("SELECT * FROM file_vaults WHERE id = :vaultId")
    LiveData<FileVaultEntity> getVaultById(long vaultId);

    @Query("SELECT * FROM file_vaults WHERE id = :vaultId")
    FileVaultEntity getVaultByIdSync(long vaultId);

    @Query("SELECT COUNT(*) FROM file_vaults")
    int getVaultCount();

    @Query("UPDATE file_vaults SET last_accessed = :timestamp WHERE id = :vaultId")
    void updateLastAccessed(long vaultId, long timestamp);
}
