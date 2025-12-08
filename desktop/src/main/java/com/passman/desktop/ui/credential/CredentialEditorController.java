package com.passman.desktop.ui.credential;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx. scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class CredentialEditorController {

    @FXML private TextField titleField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField urlField;
    @FXML private PasswordField passwordField;
    @FXML private TextArea notesArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private CredentialEditorViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new CredentialEditorViewModel();

        titleField.textProperty().bindBidirectional(viewModel.titleProperty());
        usernameField. textProperty().bindBidirectional(viewModel.usernameProperty());
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());
        urlField.textProperty().bindBidirectional(viewModel.urlProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        notesArea.textProperty().bindBidirectional(viewModel.notesProperty());
    }

    @FXML
    private void handleSave() {
        viewModel.save();
        closeDialog();
    }

    @FXML
    private void handleCancel() {
        closeDialog();
    }

    @FXML
    private void handleGenerate() {
        System.out.println("Generate password clicked");
        // TODO: Open password generator
    }

    private void closeDialog() {
        Stage stage = (Stage)saveButton.getScene().getWindow();
        stage.close();
    }
}