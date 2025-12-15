package com.passman.desktop.ui.generator;

import javafx.fxml.FXML;
import javafx.scene. control.*;
import javafx.scene.input. Clipboard;
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

    private PasswordGeneratorViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new PasswordGeneratorViewModel();

        passwordField.textProperty().bind(viewModel.generatedPasswordProperty());
        lengthSlider.valueProperty().bindBidirectional(viewModel. lengthProperty());
        uppercaseCheck.selectedProperty().bindBidirectional(viewModel. includeUppercaseProperty());
        lowercaseCheck.selectedProperty().bindBidirectional(viewModel.includeLowercaseProperty());
        numbersCheck.selectedProperty().bindBidirectional(viewModel.includeNumbersProperty());
        symbolsCheck.selectedProperty().bindBidirectional(viewModel.includeSymbolsProperty());
        ambiguousCheck. selectedProperty().bindBidirectional(viewModel.excludeAmbiguousProperty());

        lengthSlider. valueProperty().addListener((obs, old, newVal) -> {
            lengthLabel.setText(String.valueOf(newVal. intValue()));
        });
        lengthLabel.setText(String.valueOf((int) lengthSlider.getValue()));

        viewModel.generatedPasswordProperty().addListener((obs, old, newVal) -> {
            updateStrengthIndicator(newVal);
        });

        uppercaseCheck.selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        lowercaseCheck.selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        numbersCheck. selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        symbolsCheck.selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        ambiguousCheck.selectedProperty().addListener((obs, old, newVal) -> viewModel.generate());
        lengthSlider.valueProperty().addListener((obs, old, newVal) -> viewModel.generate());

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
            return;
        }

        int score = calculatePasswordStrength(password);
        double progress = score / 100.0;
        strengthBar.setProgress(progress);

        if (score >= 80) {
            strengthLabel.setText("Strong");
            strengthLabel. setStyle("-fx-text-fill: #28a745;");
        } else if (score >= 50) {
            strengthLabel.setText("Medium");
            strengthLabel. setStyle("-fx-text-fill: #ffc107;");
        } else {
            strengthLabel.setText("Weak");
            strengthLabel.setStyle("-fx-text-fill: #dc3545;");
        }
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;
        score += Math.min(password.length() * 2, 50);
        if (password.matches(".*[A-Z].*")) score += 10;
        if (password.matches(".*[a-z].*")) score += 10;
        if (password.matches(".*[0-9].*")) score += 10;
        if (password. matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;: ,.<>?].*")) score += 20;
        return Math.min(score, 100);
    }
}