package com.passman.desktop.ui.placeholder;

import com.passman.desktop.MainApp;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * Generic placeholder controller for unimplemented features
 */
public class PlaceholderController {

    @FXML private Label titleLabel;
    @FXML private Label messageLabel;

    private String featureName;

    @FXML
    public void initialize() {
        if (featureName != null) {
            titleLabel.setText(featureName);
            messageLabel.setText("The " + featureName + " feature is coming soon!");
        }
    }

    public void setFeatureName(String name) {
        this.featureName = name;
        if (titleLabel != null) {
            titleLabel.setText(name);
            messageLabel.setText("The " + name + " feature is coming soon!");
        }
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }
}