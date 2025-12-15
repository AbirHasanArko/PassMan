package com.passman.desktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene. Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages scene navigation and loading
 */
public class SceneManager {

    private final Stage primaryStage;
    private final Map<String, String> sceneMap;
    private Scene currentScene;

    public SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.sceneMap = new HashMap<>();
        initializeSceneMap();
    }

    private void initializeSceneMap() {
        sceneMap. put("Login", "/fxml/Login.fxml");
        sceneMap.put("Dashboard", "/fxml/Dashboard.fxml");
        sceneMap.put("SecureNotes", "/fxml/SecureNotes.fxml");
        sceneMap.put("IdentityCards", "/fxml/IdentityCards.fxml");
        sceneMap.put("BackupRestoreView", "/fxml/BackupRestoreView.fxml");
        sceneMap.put("QRShareView", "/fxml/QRShareView.fxml");
        sceneMap.put("GraphView", "/fxml/GraphView. fxml");
        sceneMap.put("QuizView", "/fxml/QuizView.fxml");
        sceneMap.put("AdminPanel", "/fxml/AdminPanel. fxml");
    }

    /**
     * Switch to a different scene
     */
    public void switchScene(String sceneName) {
        try {
            String fxmlPath = sceneMap.get(sceneName);
            if (fxmlPath == null) {
                throw new IllegalArgumentException("Scene not found: " + sceneName);
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            // Apply CSS
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());

            currentScene = scene;
            primaryStage.setScene(scene);

            System.out.println("✅ Switched to scene: " + sceneName);

        } catch (IOException e) {
            System.err.println("❌ Failed to load scene: " + sceneName);
            e.printStackTrace();
            DialogUtils.showError("Navigation Error", "Failed to load screen", e.getMessage());
        }
    }

    /**
     * Get current scene
     */
    public Scene getCurrentScene() {
        return currentScene;
    }

    /**
     * Get primary stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Load FXML and return root node
     */
    public Parent loadFXML(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        return loader.load();
    }

    /**
     * Load FXML with controller
     */
    public <T> T loadFXMLWithController(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        loader.load();
        return loader.getController();
    }
}