package com.passman.desktop.ui.dashboard;

import com.passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class DashboardController {

    @FXML
    private TextField searchField;

    @FXML
    private Button addButton;

    @FXML
    private TableView<DashboardViewModel.CredentialItem> credentialsTable;

    @FXML
    private PasswordTableViewController passwordTableViewController;

    private DashboardViewModel viewModel;

    @FXML
    public void initialize() {
        viewModel = new DashboardViewModel();
        searchField.textProperty().bindBidirectional(viewModel.searchQueryProperty());
    }

    @FXML
    private void handleAddCredential() {
        try {
            Stage modal = DialogUtils.openModal("/fxml/CredentialEditorDialog.fxml", "Add Credential");
            modal.showAndWait();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to open editor", e.getMessage());
        }
    }

    @FXML
    private void handleGeneratePassword() {
        try {
            Stage modal = DialogUtils.openModal("/fxml/PasswordGeneratorDialog.fxml", "Password Generator");
            modal.showAndWait();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to open generator", e.getMessage());
        }
    }

    @FXML
    private void handleBackup() {
        MainApp.getSceneManager().switchScene("BackupRestoreView");
    }

    @FXML
    private void handleQRShare() {
        MainApp.getSceneManager().switchScene("QRShareView");
    }

    @FXML
    private void handleGraph() {
        MainApp.getSceneManager().switchScene("GraphView");
    }

    @FXML
    private void handleQuiz() {
        MainApp.getSceneManager().switchScene("QuizView");
    }

    @FXML
    private void handleAdmin() {
        MainApp.getSceneManager().switchScene("AdminPanel");
    }

    @FXML
    private void handleLogout() {
        boolean confirm = DialogUtils.showConfirmation(
                "Logout",
                "Are you sure you want to logout?",
                "You will need to enter your master password again."
        );

        if (confirm) {
            MainApp.getSceneManager().switchScene("Login");
        }
    }

    public DashboardViewModel getViewModel() {
        return viewModel;
    }
}