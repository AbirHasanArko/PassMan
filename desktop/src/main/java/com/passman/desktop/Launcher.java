package com.passman.desktop;

/**
 * Launcher class for jpackage.
 * This class is needed because jpackage doesn't work well with classes that extend Application.
 */
public class Launcher {
    public static void main(String[] args) {
        MainApp.main(args);
    }
}
