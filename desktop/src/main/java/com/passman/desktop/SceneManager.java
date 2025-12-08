package com.passman.desktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene. Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages scene transitions and caching.
 */
public class SceneManager {

    private final Stage stage;
    private final Map<String, Scene> sceneCache;

    public SceneManager(Stage stage) {
        this.stage = stage;
        this.sceneCache = new HashMap<>();
    }

    public void switchScene(String sceneName) {
        try {
            Scene scene = sceneCache.get(sceneName);

            if (scene == null) {
                scene = loadScene(sceneName);
                sceneCache.put(sceneName, scene);
            }

            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Failed to load scene: " + sceneName);
            e.printStackTrace();
        }
    }

    private Scene loadScene(String sceneName) throws IOException {
        String fxmlPath = "/fxml/" + sceneName + ".fxml";
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        Scene scene = new Scene(root);

        // Load CSS
        String css = getClass().getResource("/styles/main.css").toExternalForm();
        scene.getStylesheets().add(css);

        return scene;
    }

    public void clearCache() {
        sceneCache.clear();
    }

    public Stage getStage() {
        return stage;
    }
}