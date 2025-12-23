package com.passman.desktop.ui.vault;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.FileVault;
import com.passman.core.repository.FileVaultRepositoryImpl;
import com. passman.core.services.FileVaultService;
import com. passman.desktop.DialogUtils;
import com.passman.desktop.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;

import javax.crypto.SecretKey;

/**
 * Controller for Vault Unlock Dialog
 */
public class VaultUnlockController {

    @FXML private Label vaultNameLabel;
    @FXML private Label vaultTypeLabel;
    @FXML private PasswordField passwordField;

    private FileVault vault;
    private FileVaultService vaultService;
    private SecretKey unlockedKey;

    @FXML
    public void initialize() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        FileVaultRepositoryImpl repository = new FileVaultRepositoryImpl(dbManager);
        vaultService = new FileVaultService(repository);
    }

    public void setVault(FileVault vault) {
        this.vault = vault;
        vaultNameLabel.setText(vault.getVaultName());
        vaultTypeLabel.setText(vault.getVaultType().name());

        if (! vault.isHasSeparatePassword()) {
            passwordField.setDisable(true);
            passwordField.setPromptText("Using master password");
        }
    }

    @FXML
    private void handleUnlock() {
        try {
            char[] password = null;

            if (vault.isHasSeparatePassword()) {
                String passwordText = passwordField.getText();
                if (passwordText == null || passwordText.isEmpty()) {
                    DialogUtils. showWarning("Password Required", "Enter Password",
                            "This vault requires a separate password.");
                    return;
                }
                password = passwordText.toCharArray();
            }

            unlockedKey = vaultService.unlockVault(
                    vault.getId(),
                    password,
                    SessionManager.getInstance().getMasterKey()
            );

            if (unlockedKey != null) {
                DialogUtils.showInfo("Success", "Vault Unlocked",
                        "The vault has been unlocked successfully.");
                closeDialog();
            }

        } catch (SecurityException e) {
            DialogUtils.showError("Unlock Failed", "Invalid Password",
                    "The password you entered is incorrect.");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Unlock Failed", e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) passwordField.getScene().getWindow();
        stage.close();
    }

    public SecretKey getUnlockedKey() {
        return unlockedKey;
    }
}