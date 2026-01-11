package com.passman.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.passman.android.data.entity.EncryptedFileEntity;

import java.util.List;

/**
 * Data Access Object for encrypted files
 */
@Dao
public interface EncryptedFileDao {

    @Insert
    long insert(EncryptedFileEntity file);

    @Update
    void update(EncryptedFileEntity file);

    @Delete
    void delete(EncryptedFileEntity file);

    @Query("DELETE FROM encrypted_files WHERE id = :fileId")
    void deleteById(long fileId);

    @Query("DELETE FROM encrypted_files WHERE vault_id = :vaultId")
    void deleteByVaultId(long vaultId);

    @Query("SELECT * FROM encrypted_files WHERE vault_id = :vaultId ORDER BY uploaded_at DESC")
    LiveData<List<EncryptedFileEntity>> getFilesByVaultId(long vaultId);

    @Query("SELECT * FROM encrypted_files WHERE vault_id = :vaultId ORDER BY uploaded_at DESC")
    List<EncryptedFileEntity> getFilesByVaultIdSync(long vaultId);

    @Query("SELECT * FROM encrypted_files WHERE id = :fileId")
    EncryptedFileEntity getFileById(long fileId);

    @Query("SELECT COUNT(*) FROM encrypted_files WHERE vault_id = :vaultId")
    int getFileCountByVault(long vaultId);
}
