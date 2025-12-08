package com.passman.desktop.ui.login;

import com.passman.desktop.MainApp;
import javafx.concurrent.Task;
import javafx. fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;

/**
 * Controller for the Login screen.
 */
public class LoginController {

    @FXML
    private PasswordField masterPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private ProgressIndicator progressIndicator;

    private LoginViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new LoginViewModel();

        // Bind UI to ViewModel
        masterPasswordField.textProperty().bindBidirectional(viewModel.masterPasswordProperty());
        errorLabel.textProperty().bind(viewModel.errorMessageProperty());
        progressIndicator.visibleProperty().bind(viewModel.loginInProgressProperty());
        loginButton.disableProperty().bind(viewModel.loginInProgressProperty());
    }

    @FXML
    private void handleLogin() {
        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() {
                return viewModel.login();
            }
        };

        loginTask.setOnSucceeded(event -> {
            if (loginTask.getValue()) {
                MainApp.getSceneManager().switchScene("Dashboard");
            }
        });

        new Thread(loginTask).start();
    }

    @FXML
    private void handleForgotPassword() {
        errorLabel.setText("Password recovery not yet implemented");
    }
}