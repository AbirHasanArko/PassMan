package com.passman.desktop.ui.generator;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class PasswordGeneratorController {
    @FXML private Slider lengthSlider;
    @FXML private CheckBox uppercaseCheck;
    @FXML private CheckBox lowercaseCheck;
    @FXML private CheckBox numbersCheck;
    @FXML private CheckBox symbolsCheck;
    @FXML private TextField passwordField;

    private PasswordGeneratorViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new PasswordGeneratorViewModel();
        passwordField.textProperty().bind(viewModel.generatedPasswordProperty());
    }

    @FXML
    private void handleGenerate() {
        viewModel.generate();
    }
}