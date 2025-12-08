package com.passman.desktop.ui.credential;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans. property.StringProperty;

public class CredentialEditorViewModel {
    private final StringProperty title = new SimpleStringProperty("");
    private final StringProperty username = new SimpleStringProperty("");
    private final StringProperty email = new SimpleStringProperty("");
    private final StringProperty url = new SimpleStringProperty("");
    private final StringProperty password = new SimpleStringProperty("");
    private final StringProperty notes = new SimpleStringProperty("");

    public StringProperty titleProperty() { return title; }
    public StringProperty usernameProperty() { return username; }
    public StringProperty emailProperty() { return email; }
    public StringProperty urlProperty() { return url; }
    public StringProperty passwordProperty() { return password; }
    public StringProperty notesProperty() { return notes; }

    public void save() {
        // TODO: Implement save logic
        System.out.println("Saving credential: " + title.get());
    }
}