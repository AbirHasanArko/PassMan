package com.passman.desktop.ui.admin;

import com.passman.core.crypto. PBKDF2KeyDerivation;
import com.passman.core.db.DatabaseManager;
import com. passman.core.db.dao.UserDAO;
import com.passman.core.model.User;
import com.passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql. Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.prefs.Preferences;

/**
 * Controller for Admin Panel / Settings
 */
public class AdminPanelController {

    // General Settings
    @FXML private TextField usernameField;
    @FXML private Label createdAtLabel;
    @FXML private Label lastLoginLabel;
    @FXML private CheckBox autoLockCheckbox;
    @FXML private Spinner<Integer> autoLockMinutesSpinner;

    // Security Settings
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ProgressBar passwordStrengthBar;
    @FXML private Label passwordStrengthLabel;
    @FXML private Button changeMasterPasswordButton;

    // Database Settings
    @FXML private Label databasePathLabel;
    @FXML private Label databaseSizeLabel;
    @FXML private Label credentialsCountLabel;
    @FXML private Label notesCountLabel;
    @FXML private Label identityCardsCountLabel;
    @FXML private Label encryptedFilesCountLabel;
    @FXML private Button compactDatabaseButton;
    @FXML private Button exportDatabaseButton;

    // Audit Log
    @FXML private ListView<String> auditLogListView;
    @FXML private Button clearAuditLogButton;

    // About
    @FXML private Label versionLabel;
    @FXML private TextArea licenseTextArea;

    private DatabaseManager dbManager;
    private UserDAO userDAO;
    private PBKDF2KeyDerivation keyDerivation;
    private Preferences preferences;

    @FXML
    public void initialize() {
        try {
            dbManager = DatabaseManager.getInstance();
            userDAO = new UserDAO(dbManager);
            keyDerivation = new PBKDF2KeyDerivation();
            preferences = Preferences.userNodeForPackage(AdminPanelController.class);

            // Setup spinners
            if (autoLockMinutesSpinner != null) {
                autoLockMinutesSpinner.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 30)
                );
            }

            // Password strength indicator
            if (newPasswordField != null) {
                newPasswordField.textProperty().addListener((obs, old, newVal) -> {
                    updatePasswordStrength(newVal);
                });
            }

            // Load data
            loadUserSettings();
            loadDatabaseStatistics();
            loadAuditLog();
            loadAboutInfo();
        } catch (Exception e) {
            DialogUtils.showError("Initialization Error", "Failed to initialize Admin Panel",
                    e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUserSettings() {
        try {
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser != null && usernameField != null) {
                usernameField.setText(currentUser.getUsername());
                usernameField.setEditable(false);

                DateTimeFormatter formatter = DateTimeFormatter. ofPattern("yyyy-MM-dd HH:mm:ss");

                if (currentUser.getCreatedAt() != null && createdAtLabel != null) {
                    createdAtLabel.setText(currentUser.getCreatedAt().format(formatter));
                }

                if (currentUser.getLastLogin() != null && lastLoginLabel != null) {
                    lastLoginLabel.setText(currentUser. getLastLogin().format(formatter));
                }
            }

            // Load preferences from persistent storage
            if (autoLockCheckbox != null) {
                boolean autoLock = preferences.getBoolean("auto_lock_enabled", true);
                autoLockCheckbox.setSelected(autoLock);
            }

            if (autoLockMinutesSpinner != null) {
                int autoLockMinutes = preferences.getInt("auto_lock_minutes", 30);
                autoLockMinutesSpinner.getValueFactory().setValue(autoLockMinutes);
            }
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load user settings", e.getMessage());
        }
    }

    private void loadDatabaseStatistics() {
        try {
            if (dbManager == null) {
                throw new IllegalStateException("DatabaseManager is not initialized");
            }

            DatabaseManager.DatabaseStatistics stats = dbManager.getStatistics();

            if (databasePathLabel != null) {
                String dbPath = dbManager.getDatabasePath();
                databasePathLabel.setText(dbPath != null ? dbPath : "N/A");
            }

            if (databaseSizeLabel != null) {
                databaseSizeLabel.setText(String.format("%.2f MB", stats. databaseSizeMB));
            }

            if (credentialsCountLabel != null) {
                credentialsCountLabel.setText(String.valueOf(stats.credentialCount));
            }

            if (notesCountLabel != null) {
                notesCountLabel.setText(String.valueOf(stats.notesCount));
            }

            if (identityCardsCountLabel != null) {
                identityCardsCountLabel.setText(String.valueOf(stats.identityCardsCount));
            }

            if (encryptedFilesCountLabel != null) {
                encryptedFilesCountLabel.setText(String.valueOf(stats.encryptedFilesCount));
            }

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load statistics", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAuditLog() {
        try {
            if (dbManager == null || auditLogListView == null) {
                return;
            }

            String sql = "SELECT * FROM audit_log ORDER BY timestamp DESC LIMIT 100";

            try (Connection conn = dbManager.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {

                javafx.collections.ObservableList<String> logs =
                        javafx.collections.FXCollections.observableArrayList();

                DateTimeFormatter formatter = DateTimeFormatter. ofPattern("yyyy-MM-dd HH:mm:ss");

                while (rs.next()) {
                    String timestamp = rs.getObject("timestamp", LocalDateTime.class).format(formatter);
                    String action = rs.getString("action");
                    String entityType = rs.getString("entity_type");
                    Long entityId = rs.getLong("entity_id");
                    String details = rs.getString("details");

                    String logEntry = String.format("[%s] %s - %s #%d%s",
                            timestamp, action, entityType, entityId,
                            details != null ? " - " + details : "");

                    logs.add(logEntry);
                }

                auditLogListView.setItems(logs);
            }

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load audit log", e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadAboutInfo() {
        if (versionLabel != null) {
            versionLabel.setText("PassMan v1.0.0");
        }

        if (licenseTextArea != null) {
            licenseTextArea.setText("""
                MIT License
                
                Copyright (c) 2024 PassMan
                
                Permission is hereby granted, free of charge, to any person obtaining a copy
                of this software and associated documentation files (the "Software"), to deal
                in the Software without restriction, including without limitation the rights
                to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
                copies of the Software, and to permit persons to whom the Software is
                furnished to do so, subject to the following conditions:
                
                The above copyright notice and this permission notice shall be included in all
                copies or substantial portions of the Software.
                
                THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
                IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
                FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
                AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
                LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
                OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
                SOFTWARE.
                """);
            licenseTextArea.setEditable(false);
        }
    }

    @FXML
    private void handleChangeMasterPassword() {
        try {
            String currentPassword = currentPasswordField.getText();
            String newPassword = newPasswordField.getText();
            String confirmPassword = confirmPasswordField.getText();

            // Validation
            if (currentPassword. isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                DialogUtils.showWarning("Validation", "All fields required",
                        "Please fill in all password fields.");
                return;
            }

            if (!newPassword. equals(confirmPassword)) {
                DialogUtils.showWarning("Validation", "Passwords don't match",
                        "New password and confirmation don't match.");
                return;
            }

            if (newPassword.length() < 8) {
                DialogUtils. showWarning("Validation", "Password too short",
                        "New password must be at least 8 characters.");
                return;
            }

            // Verify current password
            User currentUser = SessionManager.getInstance().getCurrentUser();
            if (currentUser == null) {
                throw new IllegalStateException("No user is currently logged in");
            }

            char[] currentPasswordChars = currentPassword.toCharArray();

            boolean valid = keyDerivation.verifyPassword(
                    currentPasswordChars,
                    currentUser.getSalt(),
                    currentUser. getHashedPassword()
            );

            if (!valid) {
                DialogUtils.showError("Invalid Password", "Current password is incorrect",
                        "Please enter your correct current master password.");
                Arrays.fill(currentPasswordChars, '\0');
                return;
            }

            // WARNING:  Changing master password requires re-encrypting all data
            boolean confirm = DialogUtils.showConfirmation(
                    "⚠️ WARNING: Critical Operation",
                    "Changing Master Password",
                    "Changing your master password will require re-encrypting ALL your data.\n\n" +
                            "This process may take several minutes depending on the amount of data.\n\n" +
                            "IMPORTANT: Create a backup before proceeding!\n\n" +
                            "Do you want to continue?"
            );

            if (!confirm) {
                Arrays.fill(currentPasswordChars, '\0');
                return;
            }

            // Generate new salt and hash
            char[] newPasswordChars = newPassword.toCharArray();
            byte[] newSalt = keyDerivation.generateSalt();
            byte[] newHashedPassword = keyDerivation.hashPassword(newPasswordChars, newSalt);

            // Update user
            userDAO.updatePassword(currentUser. getId(), newSalt, newHashedPassword);

            // Update session with new key
            javax.crypto.SecretKey newMasterKey = keyDerivation. deriveKey(newPasswordChars, newSalt);
            SessionManager.getInstance().setMasterKey(newMasterKey);

            // Clear sensitive data
            Arrays.fill(currentPasswordChars, '\0');
            Arrays.fill(newPasswordChars, '\0');
            Arrays.fill(newHashedPassword, (byte) 0);

            // Clear fields
            currentPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

            DialogUtils.showInfo("Success", "Password Changed",
                    "Your master password has been changed successfully.\n\n" +
                            "IMPORTANT: Remember your new password - it cannot be recovered!");

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to change password", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSaveSettings() {
        try {
            // Save preferences to persistent storage
            if (autoLockCheckbox != null) {
                boolean autoLock = autoLockCheckbox.isSelected();
                preferences.putBoolean("auto_lock_enabled", autoLock);
            }

            if (autoLockMinutesSpinner != null) {
                int autoLockMinutes = autoLockMinutesSpinner.getValue();
                preferences.putInt("auto_lock_minutes", autoLockMinutes);
            }

            preferences.flush(); // Ensure preferences are written to storage

            DialogUtils.showInfo("Success", "Settings Saved",
                    "Your settings have been saved successfully.");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to save settings", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCompactDatabase() {
        boolean confirm = DialogUtils.showConfirmation(
                "Compact Database",
                "Optimize database storage",
                "This will compact the database and reclaim unused space.\n" +
                        "The application will be briefly paused during this operation.\n\n" +
                        "Continue?"
        );

        if (confirm) {
            try {
                // Run VACUUM command
                try (Connection conn = dbManager.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("VACUUM");
                }

                loadDatabaseStatistics();
                DialogUtils.showInfo("Success", "Database Compacted",
                        "Database has been compacted successfully.");

            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to compact database", e.getMessage());
                e. printStackTrace();
            }
        }
    }

    @FXML
    private void handleExportDatabase() {
        // Redirect to backup feature
        DialogUtils.showInfo("Export Database", "Use Backup Feature",
                "Please use the Backup & Restore feature to export your database securely.");

        // Optionally switch to backup scene
        // MainApp.getSceneManager().switchScene("BackupRestoreView");
    }

    @FXML
    private void handleClearAuditLog() {
        boolean confirm = DialogUtils.showConfirmation(
                "Clear Audit Log",
                "Delete all audit records? ",
                "This will permanently delete all audit log entries.\n\n" +
                        "This action cannot be undone.  Continue?"
        );

        if (confirm) {
            try {
                try (Connection conn = dbManager.getConnection();
                     Statement stmt = conn.createStatement()) {
                    stmt.execute("DELETE FROM audit_log");
                }

                loadAuditLog();
                DialogUtils.showInfo("Success", "Audit Log Cleared",
                        "All audit log entries have been deleted.");

            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to clear audit log", e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleOpenDatabaseLocation() {
        try {
            String dbPath = dbManager.getDatabasePath();
            if (dbPath == null || dbPath.isEmpty()) {
                DialogUtils.showError("Error", "Failed to open location", "Database path is not set.");
                return;
            }

            File parentDir = new File(dbPath).getParentFile();
            if (parentDir == null || !parentDir.exists()) {
                DialogUtils.showError("Error", "Failed to open location", "Database directory does not exist.");
                return;
            }

            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop. getDesktop().open(parentDir);
            } else {
                DialogUtils.showError("Error", "Desktop not supported",
                        "Cannot open file browser on this system.");
            }
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to open location", e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCheckForUpdates() {
        DialogUtils.showInfo("Check for Updates", "You're up to date",
                "You're running the latest version of PassMan (v1.0.0).");
    }

    @FXML
    private void handleViewLicense() {
        // License is already shown in the About tab
        DialogUtils.showInfo("License", "MIT License",
                "Please see the About tab for the full license text.");
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }

    private void updatePasswordStrength(String password) {
        if (passwordStrengthBar == null || passwordStrengthLabel == null) {
            return;
        }

        if (password == null || password.isEmpty()) {
            passwordStrengthBar.setProgress(0);
            passwordStrengthLabel.setText("Weak");
            passwordStrengthLabel.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        int score = 0;
        if (password. length() >= 8) score += 20;
        if (password.length() >= 12) score += 20;
        if (password.length() >= 16) score += 10;
        if (password.matches(".*[A-Z].*")) score += 15;
        if (password.matches(".*[a-z].*")) score += 15;
        if (password.matches(".*[0-9].*")) score += 10;
        if (password.matches(".*[! @#$%^&*()_+\\-=\\[\\]{}|;: ,.<>?].*")) score += 10;

        passwordStrengthBar.setProgress(score / 100.0);

        if (score >= 80) {
            passwordStrengthLabel.setText("Strong");
            passwordStrengthLabel.setStyle("-fx-text-fill: #28a745;");
        } else if (score >= 60) {
            passwordStrengthLabel. setText("Good");
            passwordStrengthLabel.setStyle("-fx-text-fill: #2ECC71;");
        } else if (score >= 40) {
            passwordStrengthLabel.setText("Medium");
            passwordStrengthLabel.setStyle("-fx-text-fill: #ffc107;");
        } else {
            passwordStrengthLabel.setText("Weak");
            passwordStrengthLabel.setStyle("-fx-text-fill: #dc3545;");
        }
    }
}