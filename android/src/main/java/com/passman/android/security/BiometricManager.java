package com.passman.android.security;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import java.util.concurrent.Executor;

/**
 * Manages biometric authentication
 */
public class BiometricManager {

    private final Context context;
    private final androidx.biometric.BiometricManager biometricManager;

    public BiometricManager(Context context) {
        this.context = context;
        this.biometricManager = androidx.biometric.BiometricManager.from(context);
    }

    /**
     * Check if biometric authentication is available
     */
    public boolean isBiometricAvailable() {
        int result = biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
        );
        return result == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;
    }

    /**
     * Check if device has biometric hardware
     */
    public boolean hasBiometricHardware() {
        int result = biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
        );
        return result != androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE;
    }

    /**
     * Check if biometrics are enrolled
     */
    public boolean hasBiometricsEnrolled() {
        int result = biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
        );
        return result != androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED;
    }

    /**
     * Get status message for biometric availability
     */
    public String getBiometricStatusMessage() {
        int result = biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
        );

        switch (result) {
            case androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS:
                return "Biometric authentication available";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return "No biometric hardware available";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return "Biometric hardware currently unavailable";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return "No biometrics enrolled. Please add fingerprint in settings.";
            case androidx.biometric.BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return "Security update required for biometrics";
            default:
                return "Biometric authentication not available";
        }
    }

    /**
     * Show biometric prompt for authentication
     */
    public void authenticate(FragmentActivity activity, 
                            String title, 
                            String subtitle,
                            String negativeButtonText,
                            BiometricAuthCallback callback) {
        
        Executor executor = ContextCompat.getMainExecutor(context);

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setNegativeButtonText(negativeButtonText)
                .setConfirmationRequired(false)
                .setAllowedAuthenticators(
                        androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
                )
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(activity, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(
                            @NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        callback.onSuccess();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, 
                                                      @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                            callback.onUsePassword();
                        } else {
                            callback.onError(errorCode, errString.toString());
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        callback.onFailed();
                    }
                });

        biometricPrompt.authenticate(promptInfo);
    }

    /**
     * Callback interface for biometric authentication
     */
    public interface BiometricAuthCallback {
        void onSuccess();
        void onFailed();
        void onError(int errorCode, String errorMessage);
        void onUsePassword();
    }
}
