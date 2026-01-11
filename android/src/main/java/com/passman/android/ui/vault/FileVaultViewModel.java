package com.passman.android.ui.vault;

import android.app.Application;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.passman.android.data.entity.EncryptedFileEntity;
import com.passman.android.data.entity.FileVaultEntity;
import com.passman.android.data.repository.FileVaultRepository;
import com.passman.android.security.FileEncryptionManager;
import com.passman.core.model.EncryptedFile;
import com.passman.core.model.FileVault;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for managing file vault operations with proper database persistence
 */
public class FileVaultViewModel extends AndroidViewModel {

    private final FileVaultRepository repository;
    private final FileEncryptionManager encryptionManager;
    private final ExecutorService executorService;

    private final MutableLiveData<FileVaultEntity> currentVaultEntity = new MutableLiveData<>();
    private final MutableLiveData<List<EncryptedFile>> encryptedFiles = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();

    private long currentVaultId = -1;

    public FileVaultViewModel(@NonNull Application application) {
        super(application);
        this.repository = new FileVaultRepository(application);
        this.encryptionManager = new FileEncryptionManager(application);
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // ========== Vault List ==========

    /**
     * Get all vaults as LiveData (auto-updates from database)
     */
    public LiveData<List<FileVaultEntity>> getFileVaultsFromDb() {
        return repository.getAllVaults();
    }

    /**
     * Convert FileVaultEntity list to FileVault list for compatibility
     */
    public LiveData<List<FileVault>> getFileVaults() {
        return Transformations.map(repository.getAllVaults(), entities -> {
            List<FileVault> vaults = new ArrayList<>();
            if (entities != null) {
                for (FileVaultEntity entity : entities) {
                    vaults.add(entityToModel(entity));
                }
            }
            return vaults;
        });
    }

    // ========== Current Vault ==========

    /**
     * Set the current vault by ID and load its files
     */
    public void setCurrentVault(long vaultId) {
        this.currentVaultId = vaultId;
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                FileVaultEntity entity = repository.getVaultByIdSync(vaultId);
                if (entity != null) {
                    currentVaultEntity.postValue(entity);
                    repository.updateLastAccessed(vaultId);
                }
                loadEncryptedFilesForVault(vaultId);
            } catch (Exception e) {
                errorMessage.postValue("Failed to load vault: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public LiveData<FileVaultEntity> getCurrentVaultEntity() {
        return currentVaultEntity;
    }

    /**
     * Get current vault as FileVault model for compatibility
     */
    public LiveData<FileVault> getCurrentVault() {
        return Transformations.map(currentVaultEntity, this::entityToModel);
    }

    // ========== Vault CRUD ==========

    /**
     * Create a new vault with custom name
     */
    public void createNewVault(String vaultName, FileVault.VaultType vaultType, String emoji) {
        isLoading.setValue(true);
        
        FileVaultEntity entity = new FileVaultEntity();
        entity.setVaultName(vaultName);
        entity.setVaultType(vaultType.name());
        entity.setIconEmoji(emoji);
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setLastAccessed(System.currentTimeMillis());
        entity.setLocked(false);
        entity.setHasSeparatePassword(false);

        repository.insertVault(entity, vaultId -> {
            // Create directory for vault files
            File vaultDir = new File(getApplication().getFilesDir(), "vaults/" + vaultId);
            if (!vaultDir.exists()) {
                vaultDir.mkdirs();
            }
            successMessage.postValue("Vault created: " + vaultName);
            isLoading.postValue(false);
        });
    }

    /**
     * Delete a vault and all its files
     */
    public void deleteVault(long vaultId) {
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                // Delete physical files first
                File vaultDir = new File(getApplication().getFilesDir(), "vaults/" + vaultId);
                deleteDirectory(vaultDir);
                
                // Delete from database (cascade will delete file records)
                repository.deleteVault(vaultId);
                
                successMessage.postValue("Vault deleted");
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Failed to delete vault: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    private void deleteDirectory(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            dir.delete();
        }
    }

    // ========== File Operations ==========

    /**
     * Load encrypted files for a vault from file system
     */
    public void loadEncryptedFilesForVault(long vaultId) {
        executorService.execute(() -> {
            try {
                List<EncryptedFile> list = new ArrayList<>();
                File vaultDir = new File(getApplication().getFilesDir(), "vaults/" + vaultId);
                
                if (!vaultDir.exists()) {
                    vaultDir.mkdirs();
                }
                
                File[] files = vaultDir.listFiles((dir, name) -> name.endsWith(".enc"));
                if (files != null) {
                    for (File f : files) {
                        String encName = f.getName();
                        File meta = new File(vaultDir, encName + ".json");
                        EncryptedFile ef = new EncryptedFile();
                        ef.setVaultId(vaultId);
                        ef.setEncryptedFileName(encName);
                        ef.setEncryptedSize(f.length());
                        
                        if (meta.exists()) {
                            try (java.io.FileInputStream fis = new java.io.FileInputStream(meta)) {
                                String json = new String(fis.readAllBytes());
                                org.json.JSONObject o = new org.json.JSONObject(json);
                                ef.setOriginalFileName(o.optString("originalFileName", encName));
                                ef.setMimeType(o.optString("mimeType", null));
                                ef.setOriginalSize(o.optLong("originalSize", 0));
                                ef.setChecksum(o.optString("checksum", null));
                            } catch (Exception ignore) {}
                        } else {
                            ef.setOriginalFileName(encName.replace(".enc", ""));
                        }
                        list.add(ef);
                    }
                }
                encryptedFiles.postValue(list);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Failed to load files: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void encryptAndUploadFile(Uri fileUri, long vaultId) {
        if (fileUri == null) {
            errorMessage.setValue("Invalid file URI");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);
        successMessage.setValue(null);

        executorService.execute(() -> {
            try {
                EncryptedFile encryptedFile = encryptionManager.encryptFile(fileUri, vaultId);
                
                // Add to current list
                List<EncryptedFile> currentFiles = encryptedFiles.getValue();
                if (currentFiles == null) {
                    currentFiles = new ArrayList<>();
                }
                currentFiles.add(encryptedFile);
                encryptedFiles.postValue(new ArrayList<>(currentFiles));
                
                successMessage.postValue("File encrypted: " + encryptedFile.getOriginalFileName());
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Failed to encrypt file: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void decryptAndSaveToDownloads(EncryptedFile file) {
        if (file == null) {
            errorMessage.setValue("Invalid file");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);
        successMessage.setValue(null);

        executorService.execute(() -> {
            try {
                byte[] plain = encryptionManager.decryptFile(file);
                String displayName = file.getOriginalFileName() != null ? file.getOriginalFileName() : "passman_file";
                String mime = file.getMimeType() != null ? file.getMimeType() : "application/octet-stream";

                String savedPath;
                if (android.os.Build.VERSION.SDK_INT >= 29) {
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Downloads.DISPLAY_NAME, displayName);
                    values.put(MediaStore.Downloads.MIME_TYPE, mime);
                    values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/PassMan");
                    values.put(MediaStore.Downloads.IS_PENDING, 1);

                    Uri collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                    Uri itemUri = getApplication().getContentResolver().insert(collection, values);
                    if (itemUri == null) throw new IllegalStateException("Failed to create download entry");

                    try (OutputStream os = getApplication().getContentResolver().openOutputStream(itemUri)) {
                        if (os == null) throw new IllegalStateException("Failed to open output stream");
                        os.write(plain);
                        os.flush();
                    }

                    values.clear();
                    values.put(MediaStore.Downloads.IS_PENDING, 0);
                    getApplication().getContentResolver().update(itemUri, values, null, null);
                    savedPath = "Downloads/PassMan/" + displayName;
                } else {
                    File downloadsDir = getApplication().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    if (downloadsDir != null && !downloadsDir.exists()) {
                        downloadsDir.mkdirs();
                    }
                    File outFile = new File(downloadsDir, displayName);
                    try (FileOutputStream fos = new FileOutputStream(outFile)) {
                        fos.write(plain);
                        fos.flush();
                    }
                    savedPath = outFile.getAbsolutePath();
                }

                successMessage.postValue("Saved to: " + savedPath);
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Failed to decrypt: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public void deleteEncryptedFile(EncryptedFile file) {
        if (file == null) {
            errorMessage.setValue("Invalid file");
            return;
        }

        isLoading.setValue(true);

        executorService.execute(() -> {
            try {
                File vaultDir = new File(getApplication().getFilesDir(), "vaults/" + file.getVaultId());
                File encFile = new File(vaultDir, file.getEncryptedFileName());
                File metaFile = new File(vaultDir, file.getEncryptedFileName() + ".json");
                
                if (encFile.exists()) encFile.delete();
                if (metaFile.exists()) metaFile.delete();
                
                List<EncryptedFile> currentFiles = encryptedFiles.getValue();
                if (currentFiles != null) {
                    currentFiles.removeIf(f -> f.getEncryptedFileName().equals(file.getEncryptedFileName()));
                    encryptedFiles.postValue(new ArrayList<>(currentFiles));
                }
                
                successMessage.postValue("File deleted: " + file.getOriginalFileName());
                isLoading.postValue(false);
            } catch (Exception e) {
                errorMessage.postValue("Failed to delete file: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    // ========== LiveData Getters ==========

    public LiveData<List<EncryptedFile>> getEncryptedFiles() {
        return encryptedFiles;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    // ========== Utility ==========

    private FileVault entityToModel(FileVaultEntity entity) {
        if (entity == null) return null;
        
        FileVault vault = new FileVault();
        vault.setId(entity.getId());
        vault.setVaultName(entity.getVaultName());
        vault.setIconEmoji(entity.getIconEmoji());
        vault.setHasSeparatePassword(entity.isHasSeparatePassword());
        vault.setLocked(entity.isLocked());
        
        try {
            vault.setVaultType(FileVault.VaultType.valueOf(entity.getVaultType()));
        } catch (Exception e) {
            vault.setVaultType(FileVault.VaultType.OTHERS);
        }
        
        return vault;
    }

    /**
     * Get emoji for vault type
     */
    public static String getEmojiForType(FileVault.VaultType type) {
        switch (type) {
            case IMAGES: return "🖼️";
            case PDFS: return "📄";
            case DOCUMENTS: return "📁";
            case OTHERS: return "📦";
            case CUSTOM: return "🔐";
            default: return "📂";
        }
    }
}
