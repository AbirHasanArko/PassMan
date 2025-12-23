package com.passman.desktop.ui.qr;

import com.passman.core.services.QRCodeService;
import com.passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;

/**
 * Controller for QR Code Password Sharing
 */
public class QRShareController {

    @FXML private TextField passwordField;
    @FXML private Spinner<Integer> expirySpinner;
    @FXML private CheckBox noExpiryCheckbox;
    @FXML private ImageView qrCodeImageView;
    @FXML private Button generateButton;
    @FXML private Button saveButton;
    @FXML private Label statusLabel;
    @FXML private Label noQrCodeLabel;  // ADD this ID to FXML!

    private QRCodeService qrCodeService;
    private byte[] currentQRCodeImage;

    @FXML
    public void initialize() {
        try {
            qrCodeService = new QRCodeService();

            if (expirySpinner != null) {
                expirySpinner.setValueFactory(
                        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 60, 15)
                );
            }

            if (noExpiryCheckbox != null && expirySpinner != null) {
                noExpiryCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    expirySpinner.setDisable(newVal);
                });
            }

            if (saveButton != null) {
                saveButton.setDisable(true);
            }

            // Null-safe: set the "no qr" label to show if no image is set *and* update with changes.
            if (qrCodeImageView != null && noQrCodeLabel != null) {
                noQrCodeLabel.setVisible(qrCodeImageView.getImage() == null);
                qrCodeImageView.imageProperty().addListener((obs, oldImg, newImg) -> {
                    noQrCodeLabel.setVisible(newImg == null);
                });
            }

            System.out.println("✅ QRShareController initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize QRShareController: " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showError("Initialization Error",
                    "Failed to initialize QR Share feature", e.getMessage());
        }
    }

    @FXML
    private void handleGenerate() {
        try {
            // Validation
            if (passwordField == null || expirySpinner == null ||
                    noExpiryCheckbox == null || qrCodeService == null) {
                DialogUtils.showError("Error", "UI Not Ready",
                        "Required UI components are not initialized.");
                return;
            }

            String password = passwordField.getText();
            if (password == null || password.isEmpty()) {
                DialogUtils.showWarning("Validation", "Password Required",
                        "Please enter a password to generate QR code.");
                return;
            }

            // Security warning
            boolean confirm = DialogUtils.showConfirmation(
                    "⚠️ Security Warning",
                    "Generate QR Code for Password? ",
                    "QR codes can be scanned by anyone with visual access.\n\n" +
                            "Only generate QR codes in secure, private locations.\n" +
                            "The QR code will contain the password in encoded form.\n\n" +
                            "Do you want to continue?"
            );
            if (!confirm) {
                return;
            }

            // Generate QR code
            Integer expiryMinutes = noExpiryCheckbox.isSelected() ? 0 : expirySpinner.getValue();
            if (expiryMinutes == null) expiryMinutes = 0;

            System.out.println("Generating QR code with expiry: " + expiryMinutes + " minutes");

            currentQRCodeImage = qrCodeService.generatePasswordQRCode(password, expiryMinutes);

            if (currentQRCodeImage == null || currentQRCodeImage.length == 0) {
                throw new Exception("QR code generation returned empty data");
            }

            // Convert to JavaFX Image
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(currentQRCodeImage));
            if (bufferedImage == null) {
                throw new Exception("Failed to read generated QR code image");
            }

            Image image = SwingFXUtils.toFXImage(bufferedImage, null);

            if (qrCodeImageView != null) {
                qrCodeImageView.setImage(image);
            }

            if (saveButton != null) {
                saveButton.setDisable(false);
            }

            if (statusLabel != null) {
                statusLabel.setText(expiryMinutes > 0
                        ? "✅ QR Code generated (expires in " + expiryMinutes + " minutes)"
                        : "✅ QR Code generated (no expiry)");
                statusLabel.setStyle("-fx-text-fill: #2ECC71;");
            }

            System.out.println("✅ QR code generated successfully");

        } catch (Exception e) {
            System.err.println("❌ QR code generation failed: " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showError("Error", "Failed to generate QR code",
                    e.getMessage() + "\n\nPlease ensure ZXing library is properly included.");
        }
    }

    @FXML
    private void handleSaveQRCode() {
        try {
            if (currentQRCodeImage == null) {
                DialogUtils.showWarning("No QR Code", "Generate First",
                        "Please generate a QR code before saving.");
                return;
            }

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save QR Code");
            fileChooser.setInitialFileName("password_qr_code.png");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("PNG Image", "*.png")
            );

            File file = fileChooser.showSaveDialog(
                    qrCodeImageView != null ? qrCodeImageView.getScene().getWindow() : null
            );

            if (file != null) {
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(currentQRCodeImage));
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
        if (passwordField != null) {
            passwordField.clear();
        }
        if (qrCodeImageView != null) {
            qrCodeImageView.setImage(null);
        }
        currentQRCodeImage = null;
        if (saveButton != null) {
            saveButton.setDisable(true);
        }
        if (statusLabel != null) {
            statusLabel.setText("");
        }
        // Also make sure the noQrCodeLabel visibility updates
        if (noQrCodeLabel != null) {
            noQrCodeLabel.setVisible(true);
        }
        System.out.println("✅ QR share form cleared");
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }
}