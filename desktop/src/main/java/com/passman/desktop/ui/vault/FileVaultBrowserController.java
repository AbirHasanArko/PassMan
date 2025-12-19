package com.passman.desktop.ui. vault;

import com.passman. core.db.DatabaseManager;
import com. passman.core.model. EncryptedFile;
import com.passman.core.model.FileVault;
import com.passman.core.repository.FileVaultRepositoryImpl;
import com. passman.core.services.FileEncryptionService;
import com.passman. core.services.FileVaultService;
import com.passman. desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.crypto.SecretKey;
import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java. time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for File Vault Browser
 */
public class FileVaultBrowserController {

    @FXML private ComboBox<FileVault> vaultSelector;
    @FXML private Label vaultStatusLabel;
    @FXML private Button unlockButton;
    @FXML private Button lockButton;
    @FXML private ListView<EncryptedFile> filesListView;
    @FXML private Label fileCountLabel;
    @FXML private Label totalSizeLabel;
    @FXML private Button addFileButton;
    @FXML private Button downloadButton;
    @FXML private Button deleteButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;

    private FileVaultService vaultService;
    private FileEncryptionService fileEncryptionService;
    private FileVault currentVault;
    private SecretKey currentVaultKey;

    @FXML
    public void initialize() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        FileVaultRepositoryImpl repository = new FileVaultRepositoryImpl(dbManager);
        vaultService = new FileVaultService(repository);

        String storagePath = System.getProperty("user.home") + "/.passman";
        fileEncryptionService = new FileEncryptionService(storagePath);

        progressIndicator.setVisible(false);

        // Setup vault selector
        vaultSelector.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(FileVault vault, boolean empty) {
                super.updateItem(vault, empty);
                if (empty || vault == null) {
                    setText(null);
                } else {
                    setText(vault.getIconEmoji() + " " + vault.getVaultName() +
                            (vault.isHasSeparatePassword() ? " 🔒" : ""));
                }
            }
        });

        vaultSelector.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(FileVault vault, boolean empty) {
                super.updateItem(vault, empty);
                if (empty || vault == null) {
                    setText(null);
                } else {
                    setText(vault.getIconEmoji() + " " + vault.getVaultName());
                }
            }
        });

        // Setup files list view
        filesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(EncryptedFile file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter. ofPattern("yyyy-MM-dd HH:mm");
                    setText(file.getOriginalFileName() + " (" +
                            formatFileSize(file.getOriginalSize()) + ") - " +
                            file.getUploadedAt().format(formatter));
                }
            }
        });

        filesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            downloadButton.setDisable(newVal == null || currentVaultKey == null);
            deleteButton.setDisable(newVal == null || currentVaultKey == null);
        });

        loadVaults();
        updateUI();
    }

    private void loadVaults() {
        try {
            List<FileVault> vaults = vaultService.getAllVaults();
            vaultSelector.setItems(FXCollections.observableArrayList(vaults));

            if (! vaults.isEmpty()) {
                vaultSelector.setValue(vaults.get(0));
                handleVaultSelected();
            }
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load vaults", e.getMessage());
        }
    }

    @FXML
    private void handleVaultSelected() {
        currentVault = vaultSelector.getValue();
        if (currentVault != null) {
            // If vault has no separate password, auto-unlock
            if (!currentVault.isHasSeparatePassword()) {
                currentVaultKey = SessionManager.getInstance().getMasterKey();
                loadVaultFiles();
            } else {
                currentVaultKey = null;
                filesListView.getItems().clear();
            }
        }
        updateUI();
    }

    @FXML
    private void handleUnlockVault() {
        if (currentVault == null) return;

        try {
            if (currentVault.isHasSeparatePassword()) {
                // Show password dialog
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Unlock Vault");
                dialog. setHeaderText("Enter vault password");
                dialog.setContentText("Password:");

                dialog.showAndWait().ifPresent(password -> {
                    try {
                        currentVaultKey = vaultService.unlockVault(
                                currentVault.getId(),
                                password. toCharArray(),
                                SessionManager.getInstance().getMasterKey()
                        );
                        loadVaultFiles();
                        updateUI();
                    } catch (SecurityException e) {
                        DialogUtils.showError("Unlock Failed", "Invalid Password",
                                "The password you entered is incorrect.");
                    } catch (Exception e) {
                        DialogUtils.showError("Error", "Unlock failed", e.getMessage());
                    }
                });
            } else {
                currentVaultKey = SessionManager.getInstance().getMasterKey();
                loadVaultFiles();
                updateUI();
            }
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to unlock vault", e.getMessage());
        }
    }

    @FXML
    private void handleLockVault() {
        currentVaultKey = null;
        filesListView.getItems().clear();
        updateUI();
    }

    private void loadVaultFiles() {
        if (currentVault == null || currentVaultKey == null) return;

        try {
            List<EncryptedFile> files = getEncryptedFiles(currentVault. getId());
            filesListView. setItems(FXCollections. observableArrayList(files));

            long totalSize = files.stream().mapToLong(EncryptedFile::getOriginalSize).sum();
            fileCountLabel. setText(files.size() + " files");
            totalSizeLabel.setText(formatFileSize(totalSize));

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load files", e.getMessage());
        }
    }

    private List<EncryptedFile> getEncryptedFiles(Long vaultId) throws SQLException {
        List<EncryptedFile> files = new ArrayList<>();
        String sql = "SELECT * FROM encrypted_files WHERE vault_id = ?  ORDER BY uploaded_at DESC";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vaultId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    EncryptedFile file = new EncryptedFile();
                    file.setId(rs.getLong("id"));
                    file.setVaultId(rs.getLong("vault_id"));
                    file.setOriginalFileName(rs.getString("original_file_name"));
                    file.setEncryptedFileName(rs.getString("encrypted_file_name"));
                    file.setOriginalSize(rs.getLong("original_size"));
                    file.setEncryptedSize(rs.getLong("encrypted_size"));
                    file.setMimeType(rs.getString("mime_type"));
                    file.setEncryptionIV(rs.getBytes("encryption_iv"));
                    file.setChecksum(rs.getString("checksum"));
                    file.setUploadedAt(rs.getObject("uploaded_at", LocalDateTime. class));
                    file.setLastAccessed(rs.getObject("last_accessed", LocalDateTime. class));
                    files.add(file);
                }
            }
        }

        return files;
    }

    @FXML
    private void handleAddFile() {
        if (currentVault == null || currentVaultKey == null) {
            DialogUtils.showWarning("Vault Locked", "Unlock Vault First",
                    "Please unlock the vault before adding files.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Encrypt");
        Stage stage = (Stage) filesListView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            javafx.concurrent.Task<EncryptedFile> encryptTask = new javafx.concurrent.Task<>() {
                @Override
                protected EncryptedFile call() throws Exception {
                    return fileEncryptionService.encryptFile(file, currentVault.getId(), currentVaultKey);
                }
            };

            encryptTask.setOnRunning(e -> {
                progressIndicator. setVisible(true);
                addFileButton.setDisable(true);
                statusLabel.setText("Encrypting " + file.getName() + "...");
            });

            encryptTask.setOnSucceeded(e -> {
                progressIndicator.setVisible(false);
                addFileButton.setDisable(false);

                try {
                    EncryptedFile encryptedFile = encryptTask. getValue();
                    saveEncryptedFileMetadata(encryptedFile);
                    loadVaultFiles();
                    statusLabel.setText("File encrypted successfully");
                    DialogUtils.showInfo("Success", "File Encrypted",
                            "File has been encrypted and added to the vault.");
                } catch (Exception ex) {
                    DialogUtils.showError("Error", "Failed to save metadata", ex.getMessage());
                }
            });

            encryptTask.setOnFailed(e -> {
                progressIndicator.setVisible(false);
                addFileButton.setDisable(false);
                statusLabel.setText("Encryption failed");
                DialogUtils.showError("Error", "Encryption failed",
                        encryptTask.getException().getMessage());
            });

            new Thread(encryptTask).start();
        }
    }

    @FXML
    private void handleDownloadFile() {
        EncryptedFile selectedFile = filesListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Decrypted File");
        fileChooser.setInitialFileName(selectedFile.getOriginalFileName());
        Stage stage = (Stage) filesListView.getScene().getWindow();
        File destinationFile = fileChooser. showSaveDialog(stage);

        if (destinationFile != null) {
            javafx.concurrent.Task<File> decryptTask = new javafx.concurrent.Task<>() {
                @Override
                protected File call() throws Exception {
                    return fileEncryptionService.decryptFile(selectedFile, destinationFile, currentVaultKey);
                }
            };

            decryptTask. setOnRunning(e -> {
                progressIndicator.setVisible(true);
                downloadButton. setDisable(true);
                statusLabel.setText("Decrypting " + selectedFile.getOriginalFileName() + "...");
            });

            decryptTask.setOnSucceeded(e -> {
                progressIndicator.setVisible(false);
                downloadButton.setDisable(false);
                statusLabel.setText("File decrypted successfully");
                DialogUtils.showInfo("Success", "File Decrypted",
                        "File has been decrypted and saved to:\n" + destinationFile.getAbsolutePath());
            });

            decryptTask. setOnFailed(e -> {
                progressIndicator.setVisible(false);
                downloadButton. setDisable(false);
                statusLabel.setText("Decryption failed");
                DialogUtils. showError("Error", "Decryption failed",
                        decryptTask.getException().getMessage());
            });

            new Thread(decryptTask).start();
        }
    }

    @FXML
    private void handleDeleteFile() {
        EncryptedFile selectedFile = filesListView.getSelectionModel().getSelectedItem();
        if (selectedFile == null) return;

        boolean confirm = DialogUtils.showConfirmation(
                "Delete File",
                "Are you sure? ",
                "This will permanently delete:  " + selectedFile.getOriginalFileName()
        );

        if (confirm) {
            try {
                fileEncryptionService.deleteEncryptedFile(selectedFile);
                deleteEncryptedFileMetadata(selectedFile. getId());
                loadVaultFiles();
                DialogUtils.showInfo("Success", "File Deleted", "File has been deleted.");
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to delete file", e.getMessage());
            }
        }
    }

    @FXML
    private void handleManageVaults() {
        try {
            Stage modal = DialogUtils.openModal("/fxml/VaultSettingsDialog.fxml", "Vault Settings");
            modal.showAndWait();
            loadVaults();
        } catch (Exception e) {
            DialogUtils. showError("Error", "Failed to open settings", e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }

    private void updateUI() {
        boolean vaultSelected = currentVault != null;
        boolean vaultUnlocked = currentVaultKey != null;

        unlockButton.setDisable(! vaultSelected || vaultUnlocked);
        lockButton.setDisable(!vaultUnlocked);
        addFileButton.setDisable(!vaultUnlocked);

        if (vaultUnlocked) {
            vaultStatusLabel.setText("🔓 Unlocked");
            vaultStatusLabel.setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");
        } else if (vaultSelected) {
            vaultStatusLabel.setText("🔒 Locked");
            vaultStatusLabel.setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
        } else {
            vaultStatusLabel.setText("Select a vault");
            vaultStatusLabel.setStyle("-fx-text-fill:  #95A5A6;");
        }
    }

    private void saveEncryptedFileMetadata(EncryptedFile file) throws SQLException {
        String sql = """
            INSERT INTO encrypted_files (vault_id, original_file_name, encrypted_file_name, 
                                        original_size, encrypted_size, mime_type, encryption_iv, 
                                        checksum, uploaded_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, file.getVaultId());
            stmt.setString(2, file.getOriginalFileName());
            stmt.setString(3, file.getEncryptedFileName());
            stmt.setLong(4, file.getOriginalSize());
            stmt.setLong(5, file.getEncryptedSize());
            stmt.setString(6, file.getMimeType());
            stmt.setBytes(7, file.getEncryptionIV());
            stmt.setString(8, file.getChecksum());
            stmt.setObject(9, file.getUploadedAt());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    file.setId(rs.getLong(1));
                }
            }
        }
    }

    private void deleteEncryptedFileMetadata(Long fileId) throws SQLException {
        String sql = "DELETE FROM encrypted_files WHERE id = ?";

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, fileId);
            stmt.executeUpdate();
        }
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        return String. format("%.1f GB", bytes / (1024.0 * 1024.0 * 1024.0));
    }
}