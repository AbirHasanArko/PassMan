package com.passman.desktop.ui. qr;

import com.passman.core.services.QRCodeService;
import com.passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene. image.ImageView;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java. io.File;

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

    private QRCodeService qrCodeService;
    private byte[] currentQRCodeImage;

    @FXML
    public void initialize() {
        qrCodeService = new QRCodeService();

        expirySpinner.setValueFactory(
                new SpinnerValueFactory. IntegerSpinnerValueFactory(1, 60, 15)
        );

        noExpiryCheckbox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            expirySpinner.setDisable(newVal);
        });

        saveButton.setDisable(true);
    }

    @FXML
    private void handleGenerate() {
        String password = passwordField.getText();

        if (password.isEmpty()) {
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

        try {
            int expiryMinutes = noExpiryCheckbox.isSelected() ? 0 : expirySpinner.getValue();

            currentQRCodeImage = qrCodeService.generatePasswordQRCode(password, expiryMinutes);

            // Display QR code
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(currentQRCodeImage));
            Image image = SwingFXUtils. toFXImage(bufferedImage, null);
            qrCodeImageView.setImage(image);

            saveButton.setDisable(false);

            if (expiryMinutes > 0) {
                statusLabel.setText("✅ QR Code generated (expires in " + expiryMinutes + " minutes)");
                statusLabel.setStyle("-fx-text-fill: #2ECC71;");
            } else {
                statusLabel.setText("✅ QR Code generated (no expiry)");
                statusLabel.setStyle("-fx-text-fill: #2ECC71;");
            }

        } catch (Exception e) {
            DialogUtils. showError("Error", "Failed to generate QR code", e.getMessage());
        }
    }

    @FXML
    private void handleSaveQRCode() {
        if (currentQRCodeImage == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save QR Code");
        fileChooser.setInitialFileName("password_qr_code.png");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PNG Image", "*.png")
        );

        File file = fileChooser.showSaveDialog(qrCodeImageView.getScene().getWindow());

        if (file != null) {
            try {
                BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(currentQRCodeImage));
                ImageIO.write(bufferedImage, "PNG", file);

                DialogUtils.showInfo("Success", "QR Code Saved",
                        "QR code has been saved to:\n" + file.getAbsolutePath());

            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to save QR code", e.getMessage());
            }
        }
    }

    @FXML
    private void handleClear() {
        passwordField.clear();
        qrCodeImageView.setImage(null);
        currentQRCodeImage = null;
        saveButton.setDisable(true);
        statusLabel.setText("");
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }
}