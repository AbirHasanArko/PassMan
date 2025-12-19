package com.passman.desktop;

import com.passman.core.db.DatabaseManager;
import com.passman.desktop.utils.SessionTimeoutMonitor;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX Application Entry Point
 */
public class MainApp extends Application {

    private static SceneManager sceneManager;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize database
        try {
            DatabaseManager.getInstance().initialize();
            System.out.println("✅ Database initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize database: " + e. getMessage());
            e.printStackTrace();
            System.exit(1);
        }

        // Initialize scene manager
        sceneManager = new SceneManager(primaryStage);

        // Configure primary stage
        primaryStage.setTitle("PassMan - Secure Password Manager");
        primaryStage. setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);

        // Load login scene
        sceneManager. switchScene("Login");

        // Show stage
        primaryStage.show();

        // Start session timeout monitor
        SessionTimeoutMonitor.getInstance().start();

        // Cleanup on close
        primaryStage.setOnCloseRequest(event -> {
            cleanup();
        });
    }

    private void cleanup() {
        try {
            // Clear session
            SessionManager.getInstance().clearSession();

            // Close database connection
            DatabaseManager.getInstance().close();

            System.out.println("✅ Application shutdown complete");
        } catch (Exception e) {
            System.err.println("❌ Error during cleanup: " + e.getMessage());
        }
    }

    public static SceneManager getSceneManager() {
        return sceneManager;
    }

    public static void main(String[] args) {
        launch(args);
    }
}