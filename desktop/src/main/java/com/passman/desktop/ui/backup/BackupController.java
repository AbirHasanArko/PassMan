package com.passman.desktop.ui.backup;

import com.passman.core. db.DatabaseManager;
import com.passman.core.model. Backup;
import com.passman.core.repository.BackupRepositoryImpl;
import com. passman.core.services.BackupService;
import com.passman.core.services.BackupServiceImpl;
import com. passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx. fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for Backup and Restore
 */
public class BackupController {

    @FXML private ListView<Backup> backupsListView;
    @FXML private TextArea descriptionArea;
    @FXML private Label statsLabel;
    @FXML private Button createBackupButton;
    @FXML private Button restoreButton;
    @FXML private Button deleteButton;
    @FXML private ProgressIndicator progressIndicator;
    @FXML private Label statusLabel;

    private BackupService backupService;
    private Backup selectedBackup;

    @FXML
    public void initialize() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        BackupRepositoryImpl repository = new BackupRepositoryImpl(dbManager);

        String storagePath = System.getProperty("user.home") + "/.passman";
        backupService = new BackupServiceImpl(dbManager, repository, storagePath);

        progressIndicator.setVisible(false);

        backupsListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Backup backup, boolean empty) {
                super.updateItem(backup, empty);
                if (empty || backup == null) {
                    setText(null);
                } else {
                    DateTimeFormatter formatter = DateTimeFormatter. ofPattern("yyyy-MM-dd HH:mm");
                    setText(backup.getBackupFileName() + " - " +
                            backup.getCreatedAt().format(formatter) + " - " +
                            formatFileSize(backup.getFileSize()));
                }
            }
        });

        backupsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            selectedBackup = newVal;
            if (newVal != null) {
                descriptionArea.setText(newVal.getDescription());
                restoreButton.setDisable(false);
                deleteButton.setDisable(false);
            } else {
                descriptionArea.clear();
                restoreButton.setDisable(true);
                deleteButton.setDisable(true);
            }
        });

        loadBackups();
        updateStatistics();
    }

    private void loadBackups() {
        try {
            List<Backup> backups = backupService.getAllBackups();
            ObservableList<Backup> observableBackups = FXCollections.observableArrayList(backups);
            backupsListView.setItems(observableBackups);
            statusLabel.setText(backups.size() + " backups available");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load backups", e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            BackupService.BackupStatistics stats = backupService.getStatistics();
            statsLabel.setText(String.format(
                    "Total:  %d backups | Total Size: %s",
                    stats.totalBackups,
                    formatFileSize(stats. totalSize)
            ));
        } catch (Exception e) {
            statsLabel.setText("Statistics unavailable");
        }
    }

    @FXML
    private void handleCreateBackup() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Create Backup");
        dialog.setHeaderText("Backup Description");
        dialog.setContentText("Enter a description for this backup:");

        dialog.showAndWait().ifPresent(description -> {
            Task<Backup> backupTask = new Task<>() {
                @Override
                protected Backup call() throws Exception {
                    return backupService.createBackup(
                            SessionManager.getInstance().getMasterKey(),
                            description
                    );
                }
            };

            backupTask.setOnRunning(event -> {
                progressIndicator. setVisible(true);
                createBackupButton.setDisable(true);
                statusLabel.setText("Creating backup...");
            });

            backupTask.setOnSucceeded(event -> {
                progressIndicator.setVisible(false);
                createBackupButton.setDisable(false);

                DialogUtils.showInfo("Success", "Backup Created",
                        "Your backup has been created successfully.");

                loadBackups();
                updateStatistics();
                statusLabel.setText("Backup completed");
            });

            backupTask.setOnFailed(event -> {
                progressIndicator.setVisible(false);
                createBackupButton.setDisable(false);

                DialogUtils.showError("Error", "Backup Failed",
                        backupTask.getException().getMessage());
                statusLabel.setText("Backup failed");
            });

            new Thread(backupTask).start();
        });
    }

    @FXML
    private void handleRestore() {
        if (selectedBackup == null) {
            DialogUtils.showWarning("No Selection", "Select Backup",
                    "Please select a backup to restore.");
            return;
        }

        boolean confirm = DialogUtils.showConfirmation(
                "Restore Backup",
                "Are you sure?",
                "This will replace all current data with the backup data.\n" +
                        "Current data will be lost.  Consider creating a backup first.\n\n" +
                        "Do you want to continue?"
        );

        if (confirm) {
            File backupFile = new File(selectedBackup.getBackupPath());

            Task<Void> restoreTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    backupService.restoreBackup(
                            backupFile,
                            SessionManager.getInstance().getMasterKey()
                    );
                    return null;
                }
            };

            restoreTask.setOnRunning(event -> {
                progressIndicator.setVisible(true);
                restoreButton. setDisable(true);
                statusLabel.setText("Restoring backup...");
            });

            restoreTask.setOnSucceeded(event -> {
                progressIndicator.setVisible(false);
                restoreButton. setDisable(false);

                DialogUtils.showInfo("Success", "Restore Complete",
                        "Your data has been restored.  The application will now restart.");

                // Restart application
                SessionManager.getInstance().clearSession();
                MainApp.getSceneManager().switchScene("Login");
            });

            restoreTask.setOnFailed(event -> {
                progressIndicator.setVisible(false);
                restoreButton.setDisable(false);

                DialogUtils.showError("Error", "Restore Failed",
                        restoreTask.getException().getMessage());
                statusLabel.setText("Restore failed");
            });

            new Thread(restoreTask).start();
        }
    }

    @FXML
    private void handleDelete() {
        if (selectedBackup == null) {
            DialogUtils.showWarning("No Selection", "Select Backup",
                    "Please select a backup to delete.");
            return;
        }

        boolean confirm = DialogUtils.showConfirmation(
                "Delete Backup",
                "Are you sure?",
                "This will permanently delete the backup file:\n" +
                        selectedBackup.getBackupFileName()
        );

        if (confirm) {
            try {
                backupService.deleteBackup(selectedBackup.getId());

                DialogUtils. showInfo("Success", "Backup Deleted",
                        "The backup has been deleted.");

                loadBackups();
                updateStatistics();

            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to delete backup", e.getMessage());
            }
        }
    }

    @FXML
    private void handleExport() {
        if (selectedBackup == null) {
            DialogUtils.showWarning("No Selection", "Select Backup",
                    "Please select a backup to export.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Backup");
        fileChooser.setInitialFileName(selectedBackup.getBackupFileName());
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PassMan Backup", "*.pmbak")
        );

        Stage stage = (Stage) backupsListView.getScene().getWindow();
        File destinationFile = fileChooser. showSaveDialog(stage);

        if (destinationFile != null) {
            try {
                File sourceFile = new File(selectedBackup.getBackupPath());
                java.nio.file.Files.copy(sourceFile.toPath(), destinationFile.toPath(),
                        java.nio. file.StandardCopyOption. REPLACE_EXISTING);

                DialogUtils.showInfo("Success", "Export Complete",
                        "Backup exported successfully.");

            } catch (Exception e) {
                DialogUtils.showError("Error", "Export failed", e.getMessage());
            }
        }
    }

    @FXML
    private void handleImport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Backup");
        fileChooser.getExtensionFilters().add(
                new FileChooser. ExtensionFilter("PassMan Backup", "*.pmbak")
        );

        Stage stage = (Stage) backupsListView.getScene().getWindow();
        File backupFile = fileChooser. showOpenDialog(stage);

        if (backupFile != null) {
            boolean confirm = DialogUtils.showConfirmation(
                    "Import Backup",
                    "Restore from imported file?",
                    "Do you want to restore your data from this backup file now?"
            );

            if (confirm) {
                Task<Void> restoreTask = new Task<>() {
                    @Override
                    protected Void call() throws Exception {
                        backupService. restoreBackup(
                                backupFile,
                                SessionManager.getInstance().getMasterKey()
                        );
                        return null;
                    }
                };

                restoreTask.setOnSucceeded(event -> {
                    DialogUtils.showInfo("Success", "Import Complete",
                            "Data restored from imported backup.  Application will restart.");
                    SessionManager.getInstance().clearSession();
                    MainApp.getSceneManager().switchScene("Login");
                });

                restoreTask.setOnFailed(event -> {
                    DialogUtils.showError("Error", "Import failed",
                            restoreTask.getException().getMessage());
                });

                new Thread(restoreTask).start();
            }
        }
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}