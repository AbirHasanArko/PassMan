package com.passman.desktop.utils;

import com.passman.desktop.SessionManager;
import javafx.event.Event;
import javafx.event. EventHandler;
import javafx. scene.Scene;
import javafx.scene. input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Tracks user activity to update session timeout
 */
public class ActivityTracker {

    private static ActivityTracker instance;

    private ActivityTracker() {}

    public static ActivityTracker getInstance() {
        if (instance == null) {
            instance = new ActivityTracker();
        }
        return instance;
    }

    public void attachToScene(Scene scene) {
        EventHandler<Event> activityHandler = event -> {
            if (SessionManager.getInstance().isLoggedIn()) {
                SessionManager.getInstance().updateActivity();
            }
        };

        scene.addEventFilter(MouseEvent.ANY, activityHandler);
        scene.addEventFilter(KeyEvent. ANY, activityHandler);
    }
}