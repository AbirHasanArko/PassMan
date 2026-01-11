package com.passman.desktop;

import com.passman.core.db.DatabaseManager;
import com.passman.desktop.utils.SessionTimeoutMonitor;
import javafx.application.Application;
import javafx.scene.image.Image;
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
        primaryStage.setTitle("PassMan - All-in-One Security Solution");
        primaryStage. setMinWidth(1000);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1200);
        primaryStage.setHeight(800);

        // Set application icons (multiple sizes for different contexts)
        loadApplicationIcons(primaryStage);

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

    /**
     * Load application icons in multiple sizes for different display contexts
     */
    private void loadApplicationIcons(Stage stage) {
        String[] iconSizes = {"16", "32", "64", "128", "256"};

        for (String size : iconSizes) {
            try {
                Image icon = new Image(getClass().getResourceAsStream("/icons/icon_" + size + ".png"));
                if (!icon.isError()) {
                    stage.getIcons().add(icon);
                }
            } catch (Exception e) {
                // Icon not found, skip
            }
        }

        // Fallback: try loading a single icon.png if no sized icons found
        if (stage.getIcons().isEmpty()) {
            try {
                Image icon = new Image(getClass().getResourceAsStream("/icons/icon.png"));
                if (!icon.isError()) {
                    stage.getIcons().add(icon);
                }
            } catch (Exception e) {
                System.out.println("ℹ️ No application icons found in /icons/");
            }
        }

        if (!stage.getIcons().isEmpty()) {
            System.out.println("✅ Loaded " + stage.getIcons().size() + " application icon(s)");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}