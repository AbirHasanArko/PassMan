package com.passman.desktop.ui.credential;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.Credential;
import com.passman.core.repository.CredentialRepositoryImpl;
import com.passman.core.services.EncryptionServiceImpl;
import com.passman.desktop.DialogUtils;
import com.passman.desktop.SessionManager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Controller for Credential Editor Dialog
 */
public class CredentialEditorController {

    @FXML private TextField titleField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField urlField;

    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;

    @FXML private TextField tagsField;
    @FXML private TextArea notesField;
    @FXML private CheckBox favoriteCheckbox;

    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;

    private boolean passwordVisible = false;

    private Credential credential;
    private final CredentialRepositoryImpl repository;
    private final EncryptionServiceImpl encryptionService;

    /* =======================
       Password generator
       ======================= */
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()-_=+[]{};:,.<>?";
    private static final String ALL = LOWER + UPPER + DIGITS + SYMBOLS;
    private static final SecureRandom RANDOM = new SecureRandom();

    public CredentialEditorController() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        this.repository = new CredentialRepositoryImpl(dbManager);
        this.encryptionService = new EncryptionServiceImpl();
    }

    @FXML
    public void initialize() {
        // Strength updates from active field only
        passwordField.textProperty().addListener((obs, o, n) -> {
            if (!passwordVisible) updateStrengthIndicator(n);
        });

        passwordTextField.textProperty().addListener((obs, o, n) -> {
            if (passwordVisible) updateStrengthIndicator(n);
        });
    }

    public void setCredential(Credential credential) {
        this.credential = credential;

        if (credential != null) {
            titleField.setText(credential.getTitle());
            usernameField.setText(credential.getUsername());
            emailField.setText(credential.getEmail());
            urlField.setText(credential.getUrl());
            tagsField.setText(credential.getTags());
            notesField.setText(credential.getNotes());
            favoriteCheckbox.setSelected(credential.isFavorite());

            try {
                String decrypted = encryptionService.decryptPassword(
                        new String(credential.getEncryptedPassword()),
                        SessionManager.getInstance().getMasterKey()
                );
                passwordField.setText(decrypted);
                passwordTextField.setText(decrypted);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* =======================
       Password visibility
       ======================= */
    @FXML
    private void handleTogglePasswordVisibility() {
        if (passwordVisible) {
            passwordField.setText(passwordTextField.getText());
        } else {
            passwordTextField.setText(passwordField.getText());
        }

        passwordVisible = !passwordVisible;

        passwordField.setVisible(!passwordVisible);
        passwordField.setManaged(!passwordVisible);

        passwordTextField.setVisible(passwordVisible);
        passwordTextField.setManaged(passwordVisible);
    }

    /* =======================
       Password generation
       ======================= */
    @FXML
    private void handleGeneratePassword() {
        String generated = generateSecurePassword(16);
        passwordField.setText(generated);
        passwordTextField.setText(generated);
    }

    private String generateSecurePassword(int length) {
        StringBuilder sb = new StringBuilder(length);

        // guarantee character diversity
        sb.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        sb.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        sb.append(SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length())));

        for (int i = 4; i < length; i++) {
            sb.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }

        // shuffle characters
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }

        return new String(chars);
    }

    /* =======================
       Save / Cancel
       ======================= */
    @FXML
    private void handleSave() {
        String password = passwordVisible
                ? passwordTextField.getText()
                : passwordField.getText();

        if (titleField.getText().isEmpty() || password.isEmpty()) {
            DialogUtils.showWarning(
                    "Validation Error",
                    "Required Fields",
                    "Title and Password are required."
            );
            return;
        }

        try {
            if (credential == null) {
                credential = new Credential();
            }

            credential.setTitle(titleField.getText());
            credential.setUsername(usernameField.getText());
            credential.setEmail(emailField.getText());
            credential.setUrl(urlField.getText());
            credential.setTags(tagsField.getText());
            credential.setNotes(notesField.getText());
            credential.setFavorite(favoriteCheckbox.isSelected());
            credential.setLastModified(LocalDateTime.now());

            String encrypted = encryptionService.encryptPassword(
                    password,
                    SessionManager.getInstance().getMasterKey()
            );
            credential.setEncryptedPassword(encrypted.getBytes());

            if (credential.getId() == null) {
                repository.save(credential);
            } else {
                repository.update(credential);
            }

            handleCancel();

        } catch (Exception e) {
            DialogUtils.showError(
                    "Error",
                    "Failed to save credential",
                    e.getMessage()
            );
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    /* =======================
       Strength indicator
       ======================= */
    private void updateStrengthIndicator(String password) {
        if (password == null || password.isEmpty()) {
            strengthBar.setProgress(0);
            strengthLabel.setText("Weak");
            strengthLabel.setStyle("-fx-text-fill: #dc3545;");
            return;
        }

        int score = 0;
        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 25;
        if (password.matches(".*[A-Z].*")) score += 15;
        if (password.matches(".*[a-z].*")) score += 15;
        if (password.matches(".*[0-9].*")) score += 10;
        if (password.matches(".*[!@#$%^&*].*")) score += 10;

        strengthBar.setProgress(score / 100.0);

        if (score >= 75) {
            strengthLabel.setText("Strong");
            strengthLabel.setStyle("-fx-text-fill: #28a745;");
        } else if (score >= 50) {
            strengthLabel.setText("Medium");
            strengthLabel.setStyle("-fx-text-fill: #ffc107;");
        } else {
            strengthLabel.setText("Weak");
            strengthLabel.setStyle("-fx-text-fill: #dc3545;");
        }
    }
}