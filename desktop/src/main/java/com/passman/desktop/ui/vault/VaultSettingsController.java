package com.passman.desktop.ui.vault;

import com.passman.core.db. DatabaseManager;
import com.passman.core.model.FileVault;
import com.passman.core.repository.FileVaultRepositoryImpl;
import com. passman.core.services.FileVaultService;
import com. passman.desktop.DialogUtils;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

/**
 * Controller for Vault Settings Dialog
 */
public class VaultSettingsController {

    @FXML private ListView<FileVault> vaultsListView;
    @FXML private TextField vaultNameField;
    @FXML private ComboBox<FileVault.VaultType> vaultTypeCombo;
    @FXML private TextField iconEmojiField;
    @FXML private CheckBox separatePasswordCheck;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label statusLabel;

    private FileVaultService vaultService;
    private FileVault selectedVault;

    @FXML
    public void initialize() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        FileVaultRepositoryImpl repository = new FileVaultRepositoryImpl(dbManager);
        vaultService = new FileVaultService(repository);

        vaultTypeCombo.setItems(FXCollections.observableArrayList(FileVault.VaultType.values()));

        vaultsListView.setCellFactory(param -> new ListCell<>() {
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

        vaultsListView. getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadVaultDetails(newVal);
            }
        });

        separatePasswordCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            newPasswordField.setDisable(!newVal);
            confirmPasswordField.setDisable(!newVal);
        });

        loadAllVaults();
    }

    private void loadAllVaults() {
        try {
            List<FileVault> vaults = vaultService.getAllVaults();
            vaultsListView.setItems(FXCollections.observableArrayList(vaults));
            statusLabel.setText(vaults.size() + " vaults");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load vaults", e.getMessage());
        }
    }

    private void loadVaultDetails(FileVault vault) {
        selectedVault = vault;
        vaultNameField.setText(vault. getVaultName());
        vaultTypeCombo.setValue(vault.getVaultType());
        iconEmojiField.setText(vault.getIconEmoji());
        separatePasswordCheck.setSelected(vault.isHasSeparatePassword());
        newPasswordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleCreateVault() {
        String vaultName = vaultNameField.getText();

        if (vaultName == null || vaultName.trim().isEmpty()) {
            DialogUtils. showWarning("Validation", "Name Required", "Please enter a vault name.");
            return;
        }

        try {
            char[] password = null;

            if (separatePasswordCheck. isSelected()) {
                String newPass = newPasswordField.getText();
                String confirmPass = confirmPasswordField.getText();

                if (newPass == null || newPass.isEmpty()) {
                    DialogUtils.showWarning("Validation", "Password Required",
                            "Please enter a password for the vault.");
                    return;
                }

                if (! newPass.equals(confirmPass)) {
                    DialogUtils.showWarning("Validation", "Passwords Don't Match",
                            "The passwords you entered don't match.");
                    return;
                }

                password = newPass.toCharArray();
            }

            FileVault newVault = vaultService.createVault(
                    vaultName,
                    vaultTypeCombo.getValue(),
                    iconEmojiField.getText(),
                    password
            );

            DialogUtils.showInfo("Success", "Vault Created",
                    "The vault '" + vaultName + "' has been created successfully.");

            loadAllVaults();
            handleClear();

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to create vault", e.getMessage());
        }
    }

    @FXML
    private void handleSetPassword() {
        if (selectedVault == null) {
            DialogUtils.showWarning("No Selection", "Select Vault",
                    "Please select a vault to modify.");
            return;
        }

        String newPass = newPasswordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (newPass == null || newPass.isEmpty()) {
            DialogUtils.showWarning("Validation", "Password Required",
                    "Please enter a new password.");
            return;
        }

        if (!newPass. equals(confirmPass)) {
            DialogUtils.showWarning("Validation", "Passwords Don't Match",
                    "The passwords you entered don't match.");
            return;
        }

        try {
            vaultService.setVaultPassword(selectedVault.getId(), newPass.toCharArray());

            DialogUtils.showInfo("Success", "Password Set",
                    "The vault password has been updated.");

            loadAllVaults();

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to set password", e.getMessage());
        }
    }

    @FXML
    private void handleRemovePassword() {
        if (selectedVault == null) {
            DialogUtils.showWarning("No Selection", "Select Vault",
                    "Please select a vault to modify.");
            return;
        }

        if (! selectedVault.isHasSeparatePassword()) {
            DialogUtils.showInfo("Info", "No Password Set",
                    "This vault doesn't have a separate password.");
            return;
        }

        boolean confirm = DialogUtils.showConfirmation(
                "Remove Password",
                "Are you sure? ",
                "This will remove the separate password and use your master password instead."
        );

        if (confirm) {
            try {
                vaultService.setVaultPassword(selectedVault. getId(), null);

                DialogUtils.showInfo("Success", "Password Removed",
                        "The vault now uses your master password.");

                loadAllVaults();

            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to remove password", e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        selectedVault = null;
        vaultNameField.clear();
        vaultTypeCombo.setValue(FileVault.VaultType. OTHERS);
        iconEmojiField.clear();
        separatePasswordCheck.setSelected(false);
        newPasswordField.clear();
        confirmPasswordField.clear();
        vaultsListView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) vaultNameField.getScene().getWindow();
        stage.close();
    }
}