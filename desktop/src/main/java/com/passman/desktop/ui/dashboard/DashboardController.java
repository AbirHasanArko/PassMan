package com.passman.desktop.ui.dashboard;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.Credential;
import com.passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import com.passman.desktop.ui.credential.CredentialEditorController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.collections.FXCollections;
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
    private TableView<DashboardViewModel.CredentialItem> credentialsTable;

    // Filter controls
    @FXML
    private ComboBox<DashboardViewModel.AgeFilter> ageFilterCombo;

    @FXML
    private ComboBox<DashboardViewModel.StrengthFilter> strengthFilterCombo;

    @FXML
    private ComboBox<DashboardViewModel.SortOption> sortByCombo;

    @FXML
    private ComboBox<DashboardViewModel.SearchScope> searchScopeCombo;

    @FXML
    private CheckBox favoritesOnlyCheck;

    @FXML
    private CheckBox breachedOnlyCheck;

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

        // Setup table columns with proper cell value factories
        setupTableColumns();

        // Setup filter controls
        setupFilterControls();

        // Add row selection listener
        credentialsTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> {
                    if (newSelection != null) {
                        handleCredentialSelected(newSelection);
                    }
                }
        );

        // Add context menu
        setupContextMenu();

        // Bind search field
        searchField.textProperty().bindBidirectional(viewModel.searchQueryProperty());

        // Load credentials
        viewModel.loadCredentials();
    }

    private void setupFilterControls() {
        // Setup Search scope combo
        searchScopeCombo.setItems(FXCollections.observableArrayList(DashboardViewModel.SearchScope.values()));
        searchScopeCombo.setValue(DashboardViewModel.SearchScope.ALL);
        searchScopeCombo.valueProperty().bindBidirectional(viewModel.searchScopeProperty());

        // Setup Age filter combo
        ageFilterCombo.setItems(FXCollections.observableArrayList(DashboardViewModel.AgeFilter.values()));
        ageFilterCombo.setValue(DashboardViewModel.AgeFilter.ALL);
        ageFilterCombo.valueProperty().bindBidirectional(viewModel.ageFilterProperty());

        // Setup Strength filter combo
        strengthFilterCombo.setItems(FXCollections.observableArrayList(DashboardViewModel.StrengthFilter.values()));
        strengthFilterCombo.setValue(DashboardViewModel.StrengthFilter.ALL);
        strengthFilterCombo.valueProperty().bindBidirectional(viewModel.strengthFilterProperty());

        // Setup Sort combo
        sortByCombo.setItems(FXCollections.observableArrayList(DashboardViewModel.SortOption.values()));
        sortByCombo.setValue(DashboardViewModel.SortOption.TITLE_ASC);
        sortByCombo.valueProperty().bindBidirectional(viewModel.sortOptionProperty());

        // Setup checkboxes
        favoritesOnlyCheck.selectedProperty().bindBidirectional(viewModel.favoritesOnlyProperty());
        breachedOnlyCheck.selectedProperty().bindBidirectional(viewModel.breachedOnlyProperty());
    }

    @FXML
    private void handleFilterChange() {
        // Filters are already bound, this is for explicit refresh if needed
    }

    @FXML
    private void handleSearchScopeChange() {
        // Search scope is already bound, this is for explicit refresh if needed
    }

    @FXML
    private void handleSortChange() {
        // Sort is already bound, this is for explicit refresh if needed
    }

    @FXML
    private void handleClearFilters() {
        viewModel.clearFilters();
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
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(0);
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> usernameCol =
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(1);
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> emailCol =
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(2);
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> urlCol =
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(3);
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> ageCol =
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(4);
        @SuppressWarnings("unchecked")
        TableColumn<DashboardViewModel.CredentialItem, String> strengthCol =
                (TableColumn<DashboardViewModel.CredentialItem, String>) credentialsTable.getColumns().get(5);

        // Set cell value factories
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
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

        MenuItem shareQRItem = new MenuItem("📱 Share via QR Code");
        shareQRItem.setOnAction(e -> {
            var selected = credentialsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleShareQR(selected);
            }
        });

        contextMenu.getItems().addAll(editItem, deleteItem, new SeparatorMenuItem(), copyPasswordItem, shareQRItem);
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

    // ✅ Share credential via QR code
    private void handleShareQR(DashboardViewModel.CredentialItem item) {
        try {
            CredentialRepositoryImpl repository = new CredentialRepositoryImpl(DatabaseManager.getInstance());
            Optional<Credential> credentialOpt = repository.findById(item.getId());

            if (credentialOpt.isPresent()) {
                Credential credential = credentialOpt.get();

                // Decrypt password
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

                // Security warning
                boolean confirm = DialogUtils.showConfirmation(
                        "⚠️ Security Warning",
                        "Share '" + credential.getTitle() + "' via QR Code?",
                        "The QR code will contain the credential data including the password.\n\n" +
                                "Only share QR codes in secure, private locations.\n" +
                                "Anyone who scans this QR code will have access to this credential.\n\n" +
                                "Do you want to continue?"
                );
                if (!confirm) return;

                // Generate QR code using pure JavaFX (no AWT/Swing dependency)
                com.passman.core.services.QRCodeService qrService = new com.passman.core.services.QRCodeService();
                
                final int qrSize = 400;
                javafx.scene.image.WritableImage writableImage = new javafx.scene.image.WritableImage(qrSize, qrSize);
                javafx.scene.image.PixelWriter pixelWriter = writableImage.getPixelWriter();
                
                // Fill with white background
                for (int y = 0; y < qrSize; y++) {
                    for (int x = 0; x < qrSize; x++) {
                        pixelWriter.setColor(x, y, javafx.scene.paint.Color.WHITE);
                    }
                }
                
                // Generate QR code directly to JavaFX image (no AWT)
                qrService.generateCredentialQRCodeDirect(
                        credential.getTitle(),
                        credential.getUsername(),
                        credential.getEmail(),
                        decrypted,
                        credential.getUrl(),
                        (x, y) -> pixelWriter.setColor(x, y, javafx.scene.paint.Color.BLACK)
                );

                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(writableImage);
                imageView.setFitWidth(400);
                imageView.setFitHeight(400);
                imageView.setPreserveRatio(true);

                javafx.scene.control.Dialog<Void> dialog = new javafx.scene.control.Dialog<>();
                dialog.setTitle("Share via QR Code");
                dialog.setHeaderText("Scan this QR code with PassMan Android to import");

                javafx.scene.layout.VBox content = new javafx.scene.layout.VBox(15);
                content.setAlignment(javafx.geometry.Pos.CENTER);
                content.getChildren().addAll(
                        new javafx.scene.control.Label("📱 " + credential.getTitle()),
                        imageView,
                        new javafx.scene.control.Label("Compatible with PassMan Android app")
                );
                content.setStyle("-fx-padding: 20;");

                dialog.getDialogPane().setContent(content);
                dialog.getDialogPane().getButtonTypes().addAll(
                        javafx.scene.control.ButtonType.CLOSE,
                        new javafx.scene.control.ButtonType("💾 Save Image", javafx.scene.control.ButtonBar.ButtonData.LEFT)
                );

                // Handle save button - pass the image directly instead of bytes
                final javafx.scene.image.Image finalImage = writableImage;
                final String finalTitle = credential.getTitle();
                dialog.setResultConverter(buttonType -> {
                    if (buttonType.getButtonData() == javafx.scene.control.ButtonBar.ButtonData.LEFT) {
                        saveQRCodeImageFromFx(finalImage, finalTitle);
                    }
                    return null;
                });

                dialog.showAndWait();
            }
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to generate QR code", e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveQRCodeImageFromFx(javafx.scene.image.Image image, String title) {
        try {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Save QR Code");
            String fileName = "credential_qr_" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".png";
            fileChooser.setInitialFileName(fileName);
            fileChooser.getExtensionFilters().add(
                    new javafx.stage.FileChooser.ExtensionFilter("PNG Image", "*.png")
            );

            java.io.File file = fileChooser.showSaveDialog(credentialsTable.getScene().getWindow());
            if (file != null) {
                // Convert JavaFX Image to BufferedImage (without SwingFXUtils)
                int width = (int) image.getWidth();
                int height = (int) image.getHeight();
                java.awt.image.BufferedImage bufferedImage = new java.awt.image.BufferedImage(
                        width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                javafx.scene.image.PixelReader pixelReader = image.getPixelReader();
                
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int argb = pixelReader.getArgb(x, y);
                        bufferedImage.setRGB(x, y, argb);
                    }
                }
                
                javax.imageio.ImageIO.write(bufferedImage, "PNG", file);
                DialogUtils.showInfo("Success", "QR Code Saved", "Saved to: " + file.getAbsolutePath());
            }
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to save QR code", e.getMessage());
        }
    }

    public DashboardViewModel getViewModel() {
        return viewModel;
    }
}