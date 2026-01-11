package com.passman.desktop.ui.qr;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.Credential;
import com.passman.core.repository.CredentialRepositoryImpl;
import com.passman.core.services.EncryptionServiceImpl;
import com.passman.core.services.QRCodeService;
import com.passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Clipboard;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller for QR Code Sharing - supports both generating and scanning QR codes
 * Compatible with Android app QR format
 */
public class QRShareController {

    // Share tab controls
    @FXML private ComboBox<CredentialItem> credentialCombo;
    @FXML private TextField titleField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField urlField;
    @FXML private ImageView qrCodeImageView;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;
    @FXML private Label noQrCodeLabel;

    // Scan tab controls
    @FXML private ImageView scanPreviewImageView;
    @FXML private Label noScanImageLabel;
    @FXML private Label scanStatusLabel;
    @FXML private VBox parsedCredentialBox;
    @FXML private Label parsedTitleLabel;
    @FXML private Label parsedUsernameLabel;
    @FXML private Label parsedEmailLabel;
    @FXML private Label parsedUrlLabel;
    @FXML private Label parsedPasswordLabel;
    @FXML private StackPane scanPreviewPane;
    @FXML private TabPane mainTabPane;

    private QRCodeService qrCodeService;
    private CredentialRepositoryImpl repository;
    private EncryptionServiceImpl encryptionService;
    private byte[] currentQRCodeImage;
    private QRCodeService.CredentialData pendingImportCredential;

    @FXML
    public void initialize() {
        try {
            qrCodeService = new QRCodeService();
            repository = new CredentialRepositoryImpl(DatabaseManager.getInstance());
            encryptionService = new EncryptionServiceImpl();

            // Load credentials into combo box
            loadCredentialsIntoCombo();

            // Setup credential combo listener
            if (credentialCombo != null) {
                credentialCombo.setOnAction(e -> handleCredentialSelected());
            }

            // Setup QR code image visibility
            if (qrCodeImageView != null && noQrCodeLabel != null) {
                noQrCodeLabel.setVisible(qrCodeImageView.getImage() == null);
                qrCodeImageView.imageProperty().addListener((obs, oldImg, newImg) -> {
                    noQrCodeLabel.setVisible(newImg == null);
                });
            }

            // Setup scan preview visibility
            if (scanPreviewImageView != null && noScanImageLabel != null) {
                noScanImageLabel.setVisible(scanPreviewImageView.getImage() == null);
                scanPreviewImageView.imageProperty().addListener((obs, oldImg, newImg) -> {
                    noScanImageLabel.setVisible(newImg == null);
                });
            }

            if (saveButton != null) {
                saveButton.setDisable(true);
            }

            System.out.println("✅ QRShareController initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize QRShareController: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadCredentialsIntoCombo() {
        try {
            List<Credential> credentials = repository.findAll();
            ObservableList<CredentialItem> items = FXCollections.observableArrayList();

            for (Credential cred : credentials) {
                items.add(new CredentialItem(cred));
            }

            credentialCombo.setItems(items);
        } catch (Exception e) {
            System.err.println("Failed to load credentials: " + e.getMessage());
        }
    }

    private void handleCredentialSelected() {
        CredentialItem selected = credentialCombo.getValue();
        if (selected == null) return;

        Credential cred = selected.credential;
        titleField.setText(cred.getTitle() != null ? cred.getTitle() : "");
        usernameField.setText(cred.getUsername() != null ? cred.getUsername() : "");
        emailField.setText(cred.getEmail() != null ? cred.getEmail() : "");
        urlField.setText(cred.getUrl() != null ? cred.getUrl() : "");

        // Decrypt password
        try {
            byte[] iv = cred.getEncryptionIV();
            byte[] encrypted = cred.getEncryptedPassword();
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);
            String base64 = java.util.Base64.getEncoder().encodeToString(combined);

            String decrypted = encryptionService.decryptPassword(
                    base64,
                    SessionManager.getInstance().getMasterKey()
            );
            passwordField.setText(decrypted);
        } catch (Exception e) {
            passwordField.setText("");
            System.err.println("Failed to decrypt password: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerateCredentialQR() {
        try {
            String title = titleField.getText();
            String password = passwordField.getText();

            if ((title == null || title.isEmpty()) && (password == null || password.isEmpty())) {
                DialogUtils.showWarning("Validation", "Required Fields",
                        "Please enter at least a title or password.");
                return;
            }

            // Security warning
            boolean confirm = DialogUtils.showConfirmation(
                    "⚠️ Security Warning",
                    "Generate QR Code for Credential?",
                    "The QR code will contain the credential data including the password.\n\n" +
                            "Only generate QR codes in secure, private locations.\n" +
                            "Anyone who scans this QR code will have access to this credential.\n\n" +
                            "Do you want to continue?"
            );
            if (!confirm) {
                return;
            }

            // Generate credential QR code using pure JavaFX approach (no AWT/Swing dependency)
            // This ensures it works in packaged applications
            final int qrSize = 400;
            WritableImage writableImage = new WritableImage(qrSize, qrSize);
            PixelWriter pixelWriter = writableImage.getPixelWriter();
            
            // Fill with white background
            for (int y = 0; y < qrSize; y++) {
                for (int x = 0; x < qrSize; x++) {
                    pixelWriter.setColor(x, y, Color.WHITE);
                }
            }
            
            // Generate QR code directly to JavaFX image (bypasses AWT/Swing)
            int[] dimensions = qrCodeService.generateCredentialQRCodeDirect(
                    title,
                    usernameField.getText(),
                    emailField.getText(),
                    password,
                    urlField.getText(),
                    (x, y) -> pixelWriter.setColor(x, y, Color.BLACK)
            );

            qrCodeImageView.setImage(writableImage);
            
            // Store current QR data for later saving (don't generate PNG bytes now to avoid AWT)
            currentQRCodeImage = null; // Will be generated on-demand when saving
            
            saveButton.setDisable(false);

            statusLabel.setText("✅ QR Code generated - compatible with PassMan Android");
            statusLabel.setStyle("-fx-text-fill: #2ECC71;");

            System.out.println("✅ Credential QR code generated successfully");

        } catch (Exception e) {
            System.err.println("❌ QR code generation failed: " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showError("Error", "Failed to generate QR code", e.getMessage());
        }
    }

    @FXML
    private void handleSaveQRCode() {
        try {
            Image currentImage = qrCodeImageView.getImage();
            if (currentImage == null) {
                DialogUtils.showWarning("No QR Code", "Generate First",
                        "Please generate a QR code before saving.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save QR Code");
            String fileName = "credential_qr_" +
                    (titleField.getText().isEmpty() ? "passman" : titleField.getText().replaceAll("[^a-zA-Z0-9]", "_"))
                    + ".png";
            fileChooser.setInitialFileName(fileName);
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG Image", "*.png")
            );

            File file = fileChooser.showSaveDialog(
                    qrCodeImageView != null ? qrCodeImageView.getScene().getWindow() : null
            );

            if (file != null) {
                // Convert JavaFX Image to BufferedImage for saving (using our helper method)
                BufferedImage bufferedImage = convertFxImageToBufferedImage(currentImage);
                ImageIO.write(bufferedImage, "PNG", file);

                DialogUtils.showInfo("Success", "QR Code Saved",
                        "QR code has been saved to:\n" + file.getAbsolutePath());

                System.out.println("✅ QR code saved to: " + file.getAbsolutePath());
            }

        } catch (Exception e) {
            System.err.println("❌ Failed to save QR code: " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showError("Error", "Failed to save QR code", e.getMessage());
        }
    }

    @FXML
    private void handleClear() {
        credentialCombo.setValue(null);
        titleField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        urlField.clear();
        qrCodeImageView.setImage(null);
        currentQRCodeImage = null;
        saveButton.setDisable(true);
        statusLabel.setText("");
        System.out.println("✅ QR share form cleared");
    }

    // ========== Scan Tab Methods ==========

    @FXML
    private void handleLoadQRImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select QR Code Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(
                scanPreviewImageView != null ? scanPreviewImageView.getScene().getWindow() : null
        );

        if (file != null) {
            processQRCodeImage(file);
        }
    }

    @FXML
    private void handlePasteQRImage() {
        try {
            Clipboard clipboard = Clipboard.getSystemClipboard();

            if (clipboard.hasImage()) {
                Image image = clipboard.getImage();
                scanPreviewImageView.setImage(image);

                // Convert JavaFX Image to BufferedImage for scanning (without SwingFXUtils)
                BufferedImage bufferedImage = convertFxImageToBufferedImage(image);
                String content = qrCodeService.scanQRCodeFromImage(bufferedImage);

                processScannedContent(content);
            } else {
                DialogUtils.showWarning("No Image", "Clipboard Empty",
                        "No image found in clipboard. Copy a QR code image first.");
            }
        } catch (Exception e) {
            scanStatusLabel.setText("❌ Failed to read QR code from clipboard");
            scanStatusLabel.setStyle("-fx-text-fill: #E74C3C;");
            System.err.println("Failed to paste QR image: " + e.getMessage());
        }
    }

    private void processQRCodeImage(File file) {
        try {
            // Load image using JavaFX directly (avoids SwingFXUtils)
            Image fxImage = new Image(file.toURI().toString());
            scanPreviewImageView.setImage(fxImage);

            // Convert to BufferedImage for QR scanning
            BufferedImage bufferedImage = convertFxImageToBufferedImage(fxImage);
            String content = qrCodeService.scanQRCodeFromImage(bufferedImage);
            processScannedContent(content);

        } catch (Exception e) {
            scanStatusLabel.setText("❌ Failed to read QR code: " + e.getMessage());
            scanStatusLabel.setStyle("-fx-text-fill: #E74C3C;");
            hideParsedCredentialBox();
            System.err.println("Failed to process QR image: " + e.getMessage());
        }
    }

    private void processScannedContent(String content) {
        if (content == null || content.isEmpty()) {
            scanStatusLabel.setText("❌ No QR code found in image");
            scanStatusLabel.setStyle("-fx-text-fill: #E74C3C;");
            hideParsedCredentialBox();
            return;
        }

        System.out.println("Scanned QR content: " + content);

        // Try to parse as PassMan credential
        QRCodeService.CredentialData credData = qrCodeService.parseCredentialQR(content);

        if (credData != null) {
            // Valid PassMan credential QR
            pendingImportCredential = credData;
            showParsedCredential(credData);
            scanStatusLabel.setText("✅ PassMan credential detected");
            scanStatusLabel.setStyle("-fx-text-fill: #27AE60;");
        } else {
            // Not a PassMan QR - might be a simple password
            scanStatusLabel.setText("ℹ️ QR code found but not a PassMan credential format.\nContent: " +
                    (content.length() > 100 ? content.substring(0, 100) + "..." : content));
            scanStatusLabel.setStyle("-fx-text-fill: #F39C12;");
            hideParsedCredentialBox();
        }
    }

    private void showParsedCredential(QRCodeService.CredentialData data) {
        parsedTitleLabel.setText(data.title.isEmpty() ? "(none)" : data.title);
        parsedUsernameLabel.setText(data.username.isEmpty() ? "(none)" : data.username);
        parsedEmailLabel.setText(data.email.isEmpty() ? "(none)" : data.email);
        parsedUrlLabel.setText(data.url.isEmpty() ? "(none)" : data.url);
        parsedPasswordLabel.setText("••••••••");

        parsedCredentialBox.setVisible(true);
        parsedCredentialBox.setManaged(true);
    }

    private void hideParsedCredentialBox() {
        parsedCredentialBox.setVisible(false);
        parsedCredentialBox.setManaged(false);
        pendingImportCredential = null;
    }

    @FXML
    private void handleImportCredential() {
        if (pendingImportCredential == null) {
            DialogUtils.showWarning("No Credential", "Nothing to Import",
                    "Please scan a valid PassMan QR code first.");
            return;
        }

        boolean confirm = DialogUtils.showConfirmation(
                "Import Credential",
                "Import this credential?",
                "Title: " + pendingImportCredential.title + "\n" +
                        "Username: " + pendingImportCredential.username + "\n" +
                        "Email: " + pendingImportCredential.email + "\n\n" +
                        "This will add a new credential to your vault."
        );

        if (!confirm) return;

        try {
            // Create new credential
            Credential credential = new Credential();
            credential.setTitle(pendingImportCredential.title);
            credential.setUsername(pendingImportCredential.username);
            credential.setEmail(pendingImportCredential.email);
            credential.setUrl(pendingImportCredential.url);
            credential.setCreatedAt(LocalDateTime.now());
            credential.setLastModified(LocalDateTime.now());
            credential.setNotes("Imported via QR code");

            // Encrypt password
            String encryptedBase64 = encryptionService.encryptPassword(
                    pendingImportCredential.password,
                    SessionManager.getInstance().getMasterKey()
            );

            byte[] combined = java.util.Base64.getDecoder().decode(encryptedBase64);
            byte[] iv = new byte[12]; // GCM IV is 12 bytes
            byte[] encrypted = new byte[combined.length - 12];
            System.arraycopy(combined, 0, iv, 0, 12);
            System.arraycopy(combined, 12, encrypted, 0, encrypted.length);

            credential.setEncryptionIV(iv);
            credential.setEncryptedPassword(encrypted);

            // Save to database
            repository.save(credential);

            DialogUtils.showInfo("Success", "Credential Imported",
                    "'" + pendingImportCredential.title + "' has been added to your vault.");

            // Clear the form
            handleCancelImport();

            System.out.println("✅ Credential imported successfully: " + pendingImportCredential.title);

        } catch (Exception e) {
            System.err.println("Failed to import credential: " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showError("Error", "Failed to Import",
                    "Could not save credential: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancelImport() {
        scanPreviewImageView.setImage(null);
        scanStatusLabel.setText("");
        hideParsedCredentialBox();
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }

    // ========== Helper Methods ==========

    /**
     * Convert JavaFX Image to BufferedImage without using SwingFXUtils.
     * This works in packaged applications where javafx.swing module may not be available.
     */
    private BufferedImage convertFxImageToBufferedImage(Image image) {
        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        javafx.scene.image.PixelReader pixelReader = image.getPixelReader();
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = pixelReader.getArgb(x, y);
                bufferedImage.setRGB(x, y, argb);
            }
        }
        
        return bufferedImage;
    }

    // ========== Helper Classes ==========

    /**
     * Wrapper class for displaying credentials in ComboBox
     */
    public static class CredentialItem {
        private final Credential credential;

        public CredentialItem(Credential credential) {
            this.credential = credential;
        }

        @Override
        public String toString() {
            String display = credential.getTitle();
            if (credential.getUsername() != null && !credential.getUsername().isEmpty()) {
                display += " (" + credential.getUsername() + ")";
            }
            return display;
        }
    }
}
