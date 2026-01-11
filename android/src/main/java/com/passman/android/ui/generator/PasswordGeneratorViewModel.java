package com.passman.android.ui.generator;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.passman.android.security.CryptoManager;
import com.passman.android.security.PasswordStrengthService;

/**
 * ViewModel for Password Generator
 */
public class PasswordGeneratorViewModel extends AndroidViewModel {

    private final MutableLiveData<Integer> length = new MutableLiveData<>(16);
    private final MutableLiveData<Boolean> includeUppercase = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> includeLowercase = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> includeNumbers = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> includeSymbols = new MutableLiveData<>(true);
    private final MutableLiveData<Boolean> excludeAmbiguous = new MutableLiveData<>(false);

    private final MutableLiveData<String> generatedPassword = new MutableLiveData<>("");
    private final MutableLiveData<Integer> passwordStrength = new MutableLiveData<>(0);
    private final MutableLiveData<String> strengthLabel = new MutableLiveData<>("");
    private final MutableLiveData<String> passwordWarning = new MutableLiveData<>();
    private final MutableLiveData<Boolean> containsPersonalInfo = new MutableLiveData<>(false);

    public PasswordGeneratorViewModel(@NonNull Application application) {
        super(application);
        generate(); // Generate initial password
    }

    /**
     * Generate a new password with current settings
     */
    public void generate() {
        String password = CryptoManager.generatePassword(
                getValue(length, 16),
                getValue(includeUppercase, true),
                getValue(includeLowercase, true),
                getValue(includeNumbers, true),
                getValue(includeSymbols, true),
                getValue(excludeAmbiguous, false)
        );

        generatedPassword.setValue(password);
        updateStrength(password);
    }

    private void updateStrength(String password) {
        PasswordStrengthService.StrengthResult result = CryptoManager.getPasswordStrengthResult(password);
        passwordStrength.setValue(result.getScore());
        strengthLabel.setValue(result.getStrengthLabel());
        passwordWarning.setValue(result.getWarningMessage());
        containsPersonalInfo.setValue(result.containsPersonalInfo());
    }

    private <T> T getValue(MutableLiveData<T> liveData, T defaultValue) {
        T value = liveData.getValue();
        return value != null ? value : defaultValue;
    }

    // ==================== PRESETS ====================

    /**
     * Easy to type preset - no symbols, no ambiguous
     */
    public void setEasyPreset() {
        length.setValue(12);
        includeUppercase.setValue(true);
        includeLowercase.setValue(true);
        includeNumbers.setValue(true);
        includeSymbols.setValue(false);
        excludeAmbiguous.setValue(true);
        generate();
    }

    /**
     * Maximum security preset
     */
    public void setMaxSecurityPreset() {
        length.setValue(24);
        includeUppercase.setValue(true);
        includeLowercase.setValue(true);
        includeNumbers.setValue(true);
        includeSymbols.setValue(true);
        excludeAmbiguous.setValue(false);
        generate();
    }

    /**
     * PIN code preset
     */
    public void setPinPreset() {
        length.setValue(6);
        includeUppercase.setValue(false);
        includeLowercase.setValue(false);
        includeNumbers.setValue(true);
        includeSymbols.setValue(false);
        excludeAmbiguous.setValue(true);
        generate();
    }

    /**
     * Passphrase-style preset
     */
    public void setMemorablePreset() {
        length.setValue(16);
        includeUppercase.setValue(true);
        includeLowercase.setValue(true);
        includeNumbers.setValue(false);
        includeSymbols.setValue(false);
        excludeAmbiguous.setValue(true);
        generate();
    }

    // ==================== SETTERS ====================

    public void setLength(int value) {
        length.setValue(Math.max(4, Math.min(128, value)));
        generate();
    }

    public void setIncludeUppercase(boolean value) {
        includeUppercase.setValue(value);
        ensureAtLeastOneSelected();
        generate();
    }

    public void setIncludeLowercase(boolean value) {
        includeLowercase.setValue(value);
        ensureAtLeastOneSelected();
        generate();
    }

    public void setIncludeNumbers(boolean value) {
        includeNumbers.setValue(value);
        ensureAtLeastOneSelected();
        generate();
    }

    public void setIncludeSymbols(boolean value) {
        includeSymbols.setValue(value);
        ensureAtLeastOneSelected();
        generate();
    }

    public void setExcludeAmbiguous(boolean value) {
        excludeAmbiguous.setValue(value);
        generate();
    }

    private void ensureAtLeastOneSelected() {
        boolean upper = getValue(includeUppercase, true);
        boolean lower = getValue(includeLowercase, true);
        boolean nums = getValue(includeNumbers, true);
        boolean syms = getValue(includeSymbols, true);

        if (!upper && !lower && !nums && !syms) {
            includeLowercase.setValue(true);
        }
    }

    // ==================== GETTERS ====================

    public LiveData<Integer> getLength() {
        return length;
    }

    public LiveData<Boolean> getIncludeUppercase() {
        return includeUppercase;
    }

    public LiveData<Boolean> getIncludeLowercase() {
        return includeLowercase;
    }

    public LiveData<Boolean> getIncludeNumbers() {
        return includeNumbers;
    }

    public LiveData<Boolean> getIncludeSymbols() {
        return includeSymbols;
    }

    public LiveData<Boolean> getExcludeAmbiguous() {
        return excludeAmbiguous;
    }

    public LiveData<String> getGeneratedPassword() {
        return generatedPassword;
    }

    public LiveData<Integer> getPasswordStrength() {
        return passwordStrength;
    }

    public LiveData<String> getStrengthLabel() {
        return strengthLabel;
    }

    public LiveData<String> getPasswordWarning() {
        return passwordWarning;
    }

    public LiveData<Boolean> getContainsPersonalInfo() {
        return containsPersonalInfo;
    }

    public String getCurrentPassword() {
        return generatedPassword.getValue();
    }
}
