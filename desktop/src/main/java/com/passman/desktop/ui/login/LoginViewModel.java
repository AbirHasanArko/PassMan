package com.passman.desktop.ui.login;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx. beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * ViewModel for the Login screen.
 */
public class LoginViewModel {

    private final StringProperty masterPassword = new SimpleStringProperty("");
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty loginInProgress = new SimpleBooleanProperty(false);

    public LoginViewModel() {
    }

    public boolean login() {
        if (masterPassword.get() == null || masterPassword.get(). isEmpty()) {
            errorMessage. set("Please enter your master password");
            return false;
        }

        loginInProgress.set(true);

        // TODO: Implement actual authentication
        try {
            Thread.sleep(500); // Simulate auth delay
            loginInProgress.set(false);
            return true;
        } catch (InterruptedException e) {
            loginInProgress.set(false);
            errorMessage.set("Login failed");
            return false;
        }
    }

    // Property Getters
    public StringProperty masterPasswordProperty() { return masterPassword; }
    public StringProperty errorMessageProperty() { return errorMessage; }
    public BooleanProperty loginInProgressProperty() { return loginInProgress; }
}