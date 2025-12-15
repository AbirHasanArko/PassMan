package com.passman.desktop;

import javafx.scene.control.Alert;
import javafx.scene. control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

/**
 * Utility class for showing dialogs
 */
public class DialogUtils {

    /**
     * Show information dialog
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType. INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show warning dialog
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show error dialog
     */
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Show confirmation dialog
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Show text input dialog
     */
    public static Optional<String> showTextInput(String title, String header, String content) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(header);
        dialog.setContentText(content);
        return dialog.showAndWait();
    }

    /**
     * Show file chooser for opening
     */
    public static File showFileChooser(String title, Stage owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        return fileChooser.showOpenDialog(owner);
    }

    /**
     * Show file chooser for saving
     */
    public static File showFileSaveDialog(String title, Stage owner, String defaultFileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        if (defaultFileName != null) {
            fileChooser.setInitialFileName(defaultFileName);
        }
        return fileChooser.showSaveDialog(owner);
    }

    /**
     * Show directory chooser
     */
    public static File showDirectoryChooser(String title, Stage owner) {
        javafx.stage.DirectoryChooser directoryChooser = new javafx.stage.DirectoryChooser();
        directoryChooser.setTitle(title);
        return directoryChooser.showDialog(owner);
    }

    /**
     * Open modal dialog from FXML
     */
    public static Stage openModal(String fxmlPath, String title) throws Exception {
        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                DialogUtils.class.getResource(fxmlPath)
        );
        javafx.scene.Parent root = loader.load();

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.initModality(javafx.stage. Modality.APPLICATION_MODAL);
        stage.setScene(new javafx.scene.Scene(root));

        // Apply CSS
        stage.getScene().getStylesheets().add(
                DialogUtils.class.getResource("/styles/main.css").toExternalForm()
        );

        return stage;
    }
}