package com.passman.desktop.ui.generator;

import javafx.beans.property.*;

public class PasswordGeneratorViewModel {
    private final IntegerProperty length = new SimpleIntegerProperty(16);
    private final BooleanProperty includeUppercase = new SimpleBooleanProperty(true);
    private final BooleanProperty includeLowercase = new SimpleBooleanProperty(true);
    private final BooleanProperty includeNumbers = new SimpleBooleanProperty(true);
    private final BooleanProperty includeSymbols = new SimpleBooleanProperty(true);
    private final StringProperty generatedPassword = new SimpleStringProperty("");

    public void generate() {
        // Placeholder
        generatedPassword.set("GeneratedPass123!");
    }

    public IntegerProperty lengthProperty() { return length; }
    public BooleanProperty includeUppercaseProperty() { return includeUppercase; }
    public BooleanProperty includeLowercaseProperty() { return includeLowercase; }
    public BooleanProperty includeNumbersProperty() { return includeNumbers; }
    public BooleanProperty includeSymbolsProperty() { return includeSymbols; }
    public StringProperty generatedPasswordProperty() { return generatedPassword; }
}