package com.passman.desktop. ui. login;

import com.passman.core.crypto. PBKDF2KeyDerivation;
import com.passman. core.db.DatabaseManager;
import com. passman.core.db.dao.UserDAO;
import com.passman. core.model.User;
import com.passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import javafx.concurrent.Task;
import javafx. fxml.FXML;
import javafx.scene.control. Button;
import javafx.scene. control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene. control. ProgressIndicator;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Arrays;

/**
 * Controller for Login screen
 */
public class LoginController {

    @FXML
    private PasswordField masterPasswordField;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Button createVaultButton;

    @FXML
    private ProgressIndicator progressIndicator;

    private final DatabaseManager dbManager;
    private final UserDAO userDAO;
    private final PBKDF2KeyDerivation keyDerivation;

    public LoginController() {
        this.dbManager = DatabaseManager.getInstance();
        this.userDAO = new UserDAO(dbManager);
        this.keyDerivation = new PBKDF2KeyDerivation();
    }

    @FXML
    public void initialize() {
        errorLabel.setText("");
        progressIndicator.setVisible(false);

        // Check if vault exists
        try {
            boolean vaultExists = userDAO.userExists();
            if (vaultExists) {
                createVaultButton.setVisible(false);
            } else {
                loginButton.setVisible(false);
            }
        } catch (Exception e) {
            errorLabel. setText("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleLogin() {
        String password = masterPasswordField.getText();

        if (password == null || password.isEmpty()) {
            errorLabel.setText("Please enter your master password");
            return;
        }

        Task<Boolean> loginTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (!userDAO.userExists()) {
                    return false;
                }

                User user = userDAO.findByUsername("master").orElse(null);
                if (user == null) {
                    return false;
                }

                char[] passwordChars = password.toCharArray();
                boolean valid = keyDerivation.verifyPassword(
                        passwordChars,
                        user.getSalt(),
                        user. getHashedPassword()
                );

                if (valid) {
                    SecretKey masterKey = keyDerivation.deriveKey(passwordChars, user.getSalt());
                    SessionManager.getInstance().initSession(user, masterKey);
                    userDAO.updateLastLogin(user. getId(), LocalDateTime.now());
                }

                Arrays.fill(passwordChars, '\0');
                return valid;
            }
        };

        loginTask.setOnRunning(event -> {
            progressIndicator.setVisible(true);
            loginButton.setDisable(true);
            errorLabel.setText("");
        });

        loginTask.setOnSucceeded(event -> {
            progressIndicator.setVisible(false);
            loginButton.setDisable(false);

            if (loginTask.getValue()) {
                MainApp.getSceneManager().switchScene("Dashboard");
            } else {
                errorLabel.setText("Invalid master password");
            }
        });

        loginTask.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            loginButton.setDisable(false);
            errorLabel. setText("Login failed: " + loginTask.getException().getMessage());
        });

        new Thread(loginTask).start();
    }

    @FXML
    private void handleCreateVault() {
        String password = masterPasswordField.getText();

        if (password == null || password.isEmpty()) {
            errorLabel.setText("Please enter a master password");
            return;
        }

        if (password.length() < 8) {
            errorLabel.setText("Password must be at least 8 characters");
            return;
        }

        Task<Boolean> createTask = new Task<>() {
            @Override
            protected Boolean call() throws Exception {
                if (userDAO.userExists()) {
                    return false;
                }

                char[] passwordChars = password.toCharArray();
                byte[] salt = keyDerivation. generateSalt();
                byte[] hashedPassword = keyDerivation.hashPassword(passwordChars, salt);

                User newUser = new User("master", salt, hashedPassword);
                userDAO.create(newUser);

                SecretKey masterKey = keyDerivation.deriveKey(passwordChars, salt);
                SessionManager.getInstance().initSession(newUser, masterKey);

                Arrays.fill(passwordChars, '\0');
                Arrays.fill(hashedPassword, (byte) 0);

                return true;
            }
        };

        createTask.setOnRunning(event -> {
            progressIndicator.setVisible(true);
            createVaultButton.setDisable(true);
            errorLabel.setText("");
        });

        createTask.setOnSucceeded(event -> {
            progressIndicator.setVisible(false);
            createVaultButton.setDisable(false);

            if (createTask.getValue()) {
                DialogUtils.showInfo(
                        "Vault Created",
                        "Success!",
                        "Your secure vault has been created.  Remember your master password!"
                );
                MainApp. getSceneManager().switchScene("Dashboard");
            } else {
                errorLabel.setText("Vault already exists.  Please login instead.");
            }
        });

        createTask.setOnFailed(event -> {
            progressIndicator.setVisible(false);
            createVaultButton.setDisable(false);
            errorLabel.setText("Failed to create vault: " + createTask.getException().getMessage());
        });

        new Thread(createTask).start();
    }

    @FXML
    private void handleForgotPassword() {
        DialogUtils.showWarning(
                "Password Recovery",
                "No Recovery Available",
                "For security reasons, there is no way to recover your master password.\n\n" +
                        "If you've forgotten it, you'll need to reset your vault (all data will be lost)."
        );
    }
}