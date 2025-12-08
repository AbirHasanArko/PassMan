package com.passman.desktop;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX Application entry point for PassMan Desktop.
 */
public class MainApp extends Application {

    private static Stage primaryStage;
    private static SceneManager sceneManager;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        sceneManager = new SceneManager(stage);

        primaryStage.setTitle("PassMan - All in One Security Solution");
        primaryStage. setMinWidth(1024);
        primaryStage.setMinHeight(768);

        // Load the login scene on startup
        sceneManager.switchScene("Login");

        primaryStage.show();
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static SceneManager getSceneManager() {
        return sceneManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}