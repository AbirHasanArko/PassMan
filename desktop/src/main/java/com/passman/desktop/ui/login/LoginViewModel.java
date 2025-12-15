package com.passman.desktop. ui.login;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx. beans.property.SimpleStringProperty;
import javafx. beans.property.StringProperty;

/**
 * ViewModel for Login screen (optional MVVM pattern)
 */
public class LoginViewModel {

    private final StringProperty masterPassword = new SimpleStringProperty("");
    private final StringProperty errorMessage = new SimpleStringProperty("");
    private final BooleanProperty isLoading = new SimpleBooleanProperty(false);
    private final BooleanProperty vaultExists = new SimpleBooleanProperty(false);

    public LoginViewModel() {
    }

    // Master Password
    public String getMasterPassword() { return masterPassword.get(); }
    public void setMasterPassword(String value) { masterPassword.set(value); }
    public StringProperty masterPasswordProperty() { return masterPassword; }

    // Error Message
    public String getErrorMessage() { return errorMessage.get(); }
    public void setErrorMessage(String value) { errorMessage.set(value); }
    public StringProperty errorMessageProperty() { return errorMessage; }

    // Loading State
    public boolean isLoading() { return isLoading. get(); }
    public void setLoading(boolean value) { isLoading.set(value); }
    public BooleanProperty isLoadingProperty() { return isLoading; }

    // Vault Exists
    public boolean isVaultExists() { return vaultExists.get(); }
    public void setVaultExists(boolean value) { vaultExists.set(value); }
    public BooleanProperty vaultExistsProperty() { return vaultExists; }
}