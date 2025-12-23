package com.passman.desktop.ui.dashboard;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.Credential;
import com.passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import com.passman.desktop.ui.credential.CredentialEditorController;
import javafx.fxml.FXML;
import javafx.scene. control.Button;
import javafx.scene. control.Label;
import javafx.scene.control.TableView;
import javafx.scene. control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx. scene.Scene;
import javafx. stage.Modality;
import javafx.stage.Stage;
import com.passman.core.repository.CredentialRepositoryImpl;
import com.passman.core.services.EncryptionServiceImpl;
import java.util.Optional;

/**
 * Controller for Dashboard
 */
public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    @FXML
    private TableView<DashboardViewModel. CredentialItem> credentialsTable;

    private DashboardViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new DashboardViewModel();

        // Set master key from session
        if (SessionManager.getInstance().isLoggedIn()) {
            viewModel.setMasterKey(SessionManager.getInstance().getMasterKey());

            String username = SessionManager.getInstance().getCurrentUser().getUsername();
            welcomeLabel.setText("Welcome, " + username + "!");
        }

        // Bind table to ViewModel
        credentialsTable.setItems(viewModel.getCredentials());

        //  Setup table columns with proper cell value factories
        setupTableColumns();

        // Add row selection listener
        credentialsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        handleCredentialSelected(newSelection);
                    }
                }
        );

        // ✅ ADD THIS: Add context menu
        setupContextMenu();

        // Bind search field
        searchField.textProperty().bindBidirectional(viewModel. searchQueryProperty());

        // Load credentials
        viewModel.loadCredentials();
    }

    @FXML
    private void handleAddCredential() {
        try {
            Stage modal = DialogUtils.openModal("/fxml/CredentialEditorDialog.fxml", "Add Credential");
            modal.showAndWait();
            viewModel.loadCredentials();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to open editor", e.getMessage());
        }
    }

    @FXML
    private void handleGeneratePassword() {
        try {
            Stage modal = DialogUtils.openModal("/fxml/PasswordGeneratorDialog.fxml", "Password Generator");
            modal.showAndWait();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to open generator", e.getMessage());
        }
    }

    @FXML
    private void handleBackup() {
        MainApp.getSceneManager().switchScene("BackupRestoreView");
    }

    @FXML
    private void handleQRShare() {
        MainApp.getSceneManager().switchScene("QRShareView");
    }

    @FXML
    private void handleGraph() {
        MainApp.getSceneManager().switchScene("GraphView");
    }

    @FXML
    private void handleQuiz() {
        MainApp.getSceneManager().switchScene("QuizView");
    }

    @FXML
    private void handleAdmin() {
        MainApp.getSceneManager().switchScene("AdminPanel");
    }

    @FXML
    private void handleNotes() {
        MainApp.getSceneManager().switchScene("SecureNotes");
    }

    @FXML
    private void handleIdentityCards() {
        MainApp.getSceneManager().switchScene("IdentityCards");
    }

    @FXML
    private void handleLogout() {
        boolean confirm = DialogUtils.showConfirmation(
                "Logout",
                "Are you sure you want to logout?",
                "You will need to enter your master password again."
        );

        if (confirm) {
            SessionManager.getInstance().clearSession();
            MainApp.getSceneManager().switchScene("Login");
        }
    }

    // Add new handler method
    @FXML
    private void handleFileVaults() {
        MainApp.getSceneManager().switchScene("FileVaultBrowser");
    }

    // ✅ ADD THIS METHOD
    private void setupTableColumns() {
        // Get columns (they're defined in FXML)
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> titleCol =
                (TableColumn<DashboardViewModel. CredentialItem, String>) credentialsTable.getColumns().get(0);
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> usernameCol =
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(1);
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> urlCol =
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(2);
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> ageCol =
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(3);
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> strengthCol =
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(4);

        // Set cell value factories
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        urlCol.setCellValueFactory(new PropertyValueFactory<>("url"));
        ageCol.setCellValueFactory(new PropertyValueFactory<>("ageBadge"));
        strengthCol.setCellValueFactory(new PropertyValueFactory<>("strength"));

        // Add custom cell factories for styling
        ageCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Very Old".equals(item)) {
                        setStyle("-fx-text-fill: #E74C3C; -fx-font-weight: bold;");
                    } else if ("Old".equals(item)) {
                        setStyle("-fx-text-fill: #F39C12; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #2ECC71;");
                    }
                }
            }
        });

        strengthCol.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super. updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Strong".equals(item)) {
                        setStyle("-fx-text-fill: #2ECC71; -fx-font-weight: bold;");
                    } else if ("Medium".equals(item)) {
                        setStyle("-fx-text-fill: #F39C12;");
                    } else {
                        setStyle("-fx-text-fill: #E74C3C;");
                    }
                }
            }
        });
    }

    // ✅ ADD THIS METHOD
    private void setupContextMenu() {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem editItem = new MenuItem("Edit");
        editItem.setOnAction(e -> {
            var selected = credentialsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleEditCredential(selected);
            }
        });

        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            var selected = credentialsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleDeleteCredential(selected);
            }
        });

        MenuItem copyPasswordItem = new MenuItem("Copy Password");
        copyPasswordItem.setOnAction(e -> {
            var selected = credentialsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleCopyPassword(selected);
            }
        });

        contextMenu.getItems().addAll(editItem, deleteItem, new SeparatorMenuItem(), copyPasswordItem);
        credentialsTable.setContextMenu(contextMenu);
    }

    // ✅ ADD THIS METHOD
    private void handleCredentialSelected(DashboardViewModel. CredentialItem item) {
        // Future:  Show details in sidebar
    }

    // ✅ ADD THIS METHOD
    private void handleEditCredential(DashboardViewModel.CredentialItem item) {
        try {
            // Load credential from repository
            CredentialRepositoryImpl repository = new CredentialRepositoryImpl(DatabaseManager.getInstance());
            Optional<Credential> credentialOpt = repository.findById(item.getId());

            if (credentialOpt. isPresent()) {
                // Open editor dialog
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CredentialEditorDialog.fxml"));
                Parent root = loader.load();

                CredentialEditorController controller = loader.getController();
                controller.setCredential(credentialOpt.get());

                Stage stage = new Stage();
                stage.setTitle("Edit Credential");
                stage.initModality(Modality.APPLICATION_MODAL);
                stage. setScene(new Scene(root));
                stage.showAndWait();

                // Reload data
                viewModel.loadCredentials();
            }
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to edit credential", e. getMessage());
        }
    }

    // ✅ ADD THIS METHOD
    private void handleDeleteCredential(DashboardViewModel.CredentialItem item) {
        boolean confirm = DialogUtils.showConfirmation(
                "Delete Credential",
                "Are you sure? ",
                "This will permanently delete:  " + item.getTitle()
        );

        if (confirm) {
            try {
                CredentialRepositoryImpl repository = new CredentialRepositoryImpl(DatabaseManager.getInstance());
                repository. delete(item.getId());
                viewModel.loadCredentials();
                DialogUtils.showInfo("Success", "Deleted", "Credential has been deleted.");
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to delete", e.getMessage());
            }
        }
    }

    // ✅ ADD THIS METHOD
    private void handleCopyPassword(DashboardViewModel.CredentialItem item) {
        try {
            CredentialRepositoryImpl repository = new CredentialRepositoryImpl(DatabaseManager.getInstance());
            Optional<Credential> credentialOpt = repository.findById(item.getId());

            if (credentialOpt.isPresent()) {
                Credential credential = credentialOpt.get();

                // Decrypt password — CORRECT reconstruction of the Base64 value for decrypt
                byte[] iv = credential.getEncryptionIV();
                byte[] encrypted = credential.getEncryptedPassword();
                byte[] combined = new byte[iv.length + encrypted.length];
                System.arraycopy(iv, 0, combined, 0, iv.length);
                System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
                String base64 = java.util.Base64.getEncoder().encodeToString(combined);

                EncryptionServiceImpl encryptionService = new EncryptionServiceImpl();
                String decrypted = encryptionService.decryptPassword(
                        base64,
                        SessionManager.getInstance().getMasterKey()
                );

                // Copy to clipboard
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(decrypted);
                clipboard.setContent(content);

                // Show feedback
                DialogUtils.showInfo("Copied", "Password Copied", "Password copied to clipboard");
            }
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to copy password", e.getMessage());
        }
    }

    public DashboardViewModel getViewModel() {
        return viewModel;
    }
}