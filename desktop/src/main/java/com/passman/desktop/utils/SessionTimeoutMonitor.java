package com.passman.desktop.utils;

import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.util.Duration;

/**
 * Monitors session timeout and triggers auto-logout
 */
public class SessionTimeoutMonitor {

    private static SessionTimeoutMonitor instance;
    private Timeline timeline;
    private static final int CHECK_INTERVAL_SECONDS = 60; // Check every minute

    private SessionTimeoutMonitor() {
        initializeMonitor();
    }

    public static SessionTimeoutMonitor getInstance() {
        if (instance == null) {
            instance = new SessionTimeoutMonitor();
        }
        return instance;
    }

    private void initializeMonitor() {
        timeline = new Timeline(new KeyFrame(Duration.seconds(CHECK_INTERVAL_SECONDS), event -> {
            checkTimeout();
        }));
        timeline.setCycleCount(Timeline. INDEFINITE);
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline. stop();
    }

    private void checkTimeout() {
        if (!SessionManager.getInstance().isLoggedIn()) {
            stop();
            return;
        }

        if (SessionManager.getInstance().isSessionExpired()) {
            Platform. runLater(() -> {
                handleTimeout();
            });
        }
    }

    private void handleTimeout() {
        SessionManager.getInstance().clearSession();

        // Show timeout message
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                javafx.scene.control.Alert. AlertType.WARNING
        );
        alert.setTitle("Session Timeout");
        alert.setHeaderText("Your session has expired");
        alert.setContentText("For your security, you have been logged out due to inactivity.");
        alert.showAndWait();

        // Return to login
        MainApp.getSceneManager().switchScene("Login");
        stop();
    }
}