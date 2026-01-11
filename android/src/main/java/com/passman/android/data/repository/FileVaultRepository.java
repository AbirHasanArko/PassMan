package com.passman.android.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.passman.android.data.dao.EncryptedFileDao;
import com.passman.android.data.dao.FileVaultDao;
import com.passman.android.data.database.PassManDatabase;
import com.passman.android.data.entity.EncryptedFileEntity;
import com.passman.android.data.entity.FileVaultEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for file vault operations
 */
public class FileVaultRepository {

    private final FileVaultDao vaultDao;
    private final EncryptedFileDao fileDao;
    private final ExecutorService executorService;

    public FileVaultRepository(Application application) {
        PassManDatabase database = PassManDatabase.getInstance(application);
        this.vaultDao = database.fileVaultDao();
        this.fileDao = database.encryptedFileDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // ========== Vault Operations ==========

    public LiveData<List<FileVaultEntity>> getAllVaults() {
        return vaultDao.getAllVaults();
    }

    public List<FileVaultEntity> getAllVaultsSync() {
        return vaultDao.getAllVaultsSync();
    }

    public LiveData<FileVaultEntity> getVaultById(long vaultId) {
        return vaultDao.getVaultById(vaultId);
    }

    public FileVaultEntity getVaultByIdSync(long vaultId) {
        return vaultDao.getVaultByIdSync(vaultId);
    }

    public void insertVault(FileVaultEntity vault, OnVaultInsertedCallback callback) {
        executorService.execute(() -> {
            long id = vaultDao.insert(vault);
            if (callback != null) {
                callback.onVaultInserted(id);
            }
        });
    }

    public void updateVault(FileVaultEntity vault) {
        executorService.execute(() -> vaultDao.update(vault));
    }

    public void deleteVault(long vaultId) {
        executorService.execute(() -> vaultDao.deleteById(vaultId));
    }

    public void updateLastAccessed(long vaultId) {
        executorService.execute(() -> vaultDao.updateLastAccessed(vaultId, System.currentTimeMillis()));
    }

    // ========== File Operations ==========

    public LiveData<List<EncryptedFileEntity>> getFilesByVaultId(long vaultId) {
        return fileDao.getFilesByVaultId(vaultId);
    }

    public List<EncryptedFileEntity> getFilesByVaultIdSync(long vaultId) {
        return fileDao.getFilesByVaultIdSync(vaultId);
    }

    public void insertFile(EncryptedFileEntity file, OnFileInsertedCallback callback) {
        executorService.execute(() -> {
            long id = fileDao.insert(file);
            if (callback != null) {
                callback.onFileInserted(id);
            }
        });
    }

    public void deleteFile(long fileId) {
        executorService.execute(() -> fileDao.deleteById(fileId));
    }

    public void deleteFilesByVaultId(long vaultId) {
        executorService.execute(() -> fileDao.deleteByVaultId(vaultId));
    }

    public int getFileCountByVault(long vaultId) {
        return fileDao.getFileCountByVault(vaultId);
    }

    // ========== Callbacks ==========

    public interface OnVaultInsertedCallback {
        void onVaultInserted(long vaultId);
    }

    public interface OnFileInsertedCallback {
        void onFileInserted(long fileId);
    }
}
