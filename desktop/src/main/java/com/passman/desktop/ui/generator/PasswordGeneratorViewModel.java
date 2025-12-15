package com.passman.desktop.ui.generator;

import javafx.beans.property.*;

import java.security.SecureRandom;

/**
 * ViewModel for Password Generator
 */
public class PasswordGeneratorViewModel {
    private final IntegerProperty length = new SimpleIntegerProperty(16);
    private final BooleanProperty includeUppercase = new SimpleBooleanProperty(true);
    private final BooleanProperty includeLowercase = new SimpleBooleanProperty(true);
    private final BooleanProperty includeNumbers = new SimpleBooleanProperty(true);
    private final BooleanProperty includeSymbols = new SimpleBooleanProperty(true);
    private final BooleanProperty excludeAmbiguous = new SimpleBooleanProperty(false);
    private final StringProperty generatedPassword = new SimpleStringProperty("");

    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBERS = "0123456789";
    private static final String SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,. <>?";
    private static final String AMBIGUOUS = "0O1lI";

    private final SecureRandom random = new SecureRandom();

    public PasswordGeneratorViewModel() {
        generate();
    }

    public void generate() {
        StringBuilder charset = new StringBuilder();
        StringBuilder password = new StringBuilder();

        if (includeUppercase.get()) charset.append(UPPERCASE);
        if (includeLowercase. get()) charset.append(LOWERCASE);
        if (includeNumbers.get()) charset.append(NUMBERS);
        if (includeSymbols.get()) charset.append(SYMBOLS);

        if (excludeAmbiguous.get()) {
            for (char c : AMBIGUOUS.toCharArray()) {
                int index = charset.indexOf(String.valueOf(c));
                if (index != -1) {
                    charset.deleteCharAt(index);
                }
            }
        }

        if (charset.length() == 0) {
            charset. append(LOWERCASE);
        }

        for (int i = 0; i < length.get(); i++) {
            int randomIndex = random.nextInt(charset.length());
            password. append(charset.charAt(randomIndex));
        }

        generatedPassword.set(password. toString());
    }

    public void setEasyPreset() {
        length.set(12);
        includeUppercase. set(true);
        includeLowercase.set(true);
        includeNumbers.set(true);
        includeSymbols.set(false);
        excludeAmbiguous.set(true);
        generate();
    }

    public void setMaxSecurityPreset() {
        length.set(32);
        includeUppercase. set(true);
        includeLowercase. set(true);
        includeNumbers.set(true);
        includeSymbols.set(true);
        excludeAmbiguous.set(false);
        generate();
    }

    public void setPinPreset() {
        length.set(6);
        includeUppercase. set(false);
        includeLowercase.set(false);
        includeNumbers.set(true);
        includeSymbols.set(false);
        excludeAmbiguous.set(false);
        generate();
    }

    public IntegerProperty lengthProperty() { return length; }
    public BooleanProperty includeUppercaseProperty() { return includeUppercase; }
    public BooleanProperty includeLowercaseProperty() { return includeLowercase; }
    public BooleanProperty includeNumbersProperty() { return includeNumbers; }
    public BooleanProperty includeSymbolsProperty() { return includeSymbols; }
    public BooleanProperty excludeAmbiguousProperty() { return excludeAmbiguous; }
    public StringProperty generatedPasswordProperty() { return generatedPassword; }
}