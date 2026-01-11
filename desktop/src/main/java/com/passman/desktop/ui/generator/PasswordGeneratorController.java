package com.passman.desktop.ui.generator;

import com.passman.core.services.PasswordStrengthService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

/**
 * Controller for Password Generator
 */
public class PasswordGeneratorController {
    @FXML private Slider lengthSlider;
    @FXML private Label lengthLabel;
    @FXML private CheckBox uppercaseCheck;
    @FXML private CheckBox lowercaseCheck;
    @FXML private CheckBox numbersCheck;
    @FXML private CheckBox symbolsCheck;
    @FXML private CheckBox ambiguousCheck;
    @FXML private TextField passwordField;
    @FXML private ProgressBar strengthBar;
    @FXML private Label strengthLabel;
    @FXML private Label warningLabel;

    private PasswordGeneratorViewModel viewModel;
    private final PasswordStrengthService passwordStrengthService;

    public PasswordGeneratorController() {
        this.passwordStrengthService = new PasswordStrengthService();
    }

    @FXML
    public void initialize() {
        viewModel = new PasswordGeneratorViewModel();

        passwordField.textProperty().bind(viewModel.generatedPasswordProperty());
        lengthSlider.valueProperty().bindBidirectional(viewModel.lengthProperty());
        uppercaseCheck.selectedProperty().bindBidirectional(viewModel.includeUppercaseProperty());
        lowercaseCheck.selectedProperty().bindBidirectional(viewModel.includeLowercaseProperty());
        numbersCheck.selectedProperty().bindBidirectional(viewModel.includeNumbersProperty());
        symbolsCheck.selectedProperty().bindBidirectional(viewModel.includeSymbolsProperty());
        ambiguousCheck.selectedProperty().bindBidirectional(viewModel.excludeAmbiguousProperty());

        lengthSlider.valueProperty().addListener((obs, old, newVal) -> {
            lengthLabel.setText(String.valueOf(newVal.intValue()));
        });
        lengthLabel.setText(String.valueOf((int) lengthSlider.getValue()));

        viewModel.generatedPasswordProperty().addListener((obs, old, newVal) -> {
            updateStrengthIndicator(newVal);
        });

        uppercaseCheck.selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        lowercaseCheck.selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        numbersCheck.selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        symbolsCheck.selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        ambiguousCheck.selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        lengthSlider.valueProperty().addListener((obs, old, newVal) -> viewModel.generate());

        // Initialize warning label visibility
        if (warningLabel != null) {
            warningLabel.setVisible(false);
            warningLabel.setManaged(false);
        }

        updateStrengthIndicator(passwordField.getText());
    }

    @FXML
    private void handleGenerate() {
        viewModel.generate();
    }

    @FXML
    private void handleCopy() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(passwordField.getText());
        clipboard.setContent(content);

        passwordField.setStyle("-fx-background-color: #d4edda;");
        new Thread(() -> {
            try {
                Thread.sleep(500);
                javafx.application.Platform.runLater(() ->
                        passwordField.setStyle("")
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void handleEasyPreset() {
        viewModel.setEasyPreset();
    }

    @FXML
    private void handleMaxPreset() {
        viewModel. setMaxSecurityPreset();
    }

    @FXML
    private void handlePinPreset() {
        viewModel.setPinPreset();
    }

    @FXML
    private void handleUsePassword() {
        handleClose();
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) passwordField.getScene().getWindow();
        stage.close();
    }

    private void updateStrengthIndicator(String password) {
        if (password == null || password.isEmpty()) {
            strengthBar.setProgress(0);
            strengthLabel.setText("Weak");
            strengthLabel.setStyle("-fx-text-fill: #dc3545;");
            hideWarning();
            return;
        }

        // Calculate strength with personal info detection (no context for generator)
        PasswordStrengthService.StrengthResult result = passwordStrengthService.calculateStrength(password);
        int score = result.getScore();
        double progress = score / 100.0;
        strengthBar.setProgress(progress);

        if (score >= 80) {
            strengthLabel.setText("Strong");
            strengthLabel.setStyle("-fx-text-fill: #28a745;");
        } else if (score >= 50) {
            strengthLabel.setText("Medium");
            strengthLabel.setStyle("-fx-text-fill: #ffc107;");
        } else {
            strengthLabel.setText("Weak");
            strengthLabel.setStyle("-fx-text-fill: #dc3545;");
        }

        // Show warnings if problematic patterns detected
        if (result.hasWarnings()) {
            showWarning(String.join("\n", result.getWarnings()));
        } else {
            hideWarning();
        }
    }

    private void showWarning(String message) {
        if (warningLabel != null) {
            warningLabel.setText(message);
            warningLabel.setStyle("-fx-text-fill: #ff6600; -fx-font-weight: bold;");
            warningLabel.setVisible(true);
            warningLabel.setManaged(true);
        }
    }

    private void hideWarning() {
        if (warningLabel != null) {
            warningLabel.setVisible(false);
            warningLabel.setManaged(false);
        }
    }

    private int calculatePasswordStrength(String password) {
        return passwordStrengthService.getScore(password);
    }
}