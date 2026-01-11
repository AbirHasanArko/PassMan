package com.passman.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.passman.android.PassManApp;
import com.passman.android.R;
import com.passman.android.databinding.ActivityAuthBinding;
import com.passman.android.security.BiometricManager;
import com.passman.android.ui.main.MainActivity;

/**
 * Authentication Activity for login and vault creation
 */
public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private AuthViewModel viewModel;
    private BiometricManager biometricManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        biometricManager = ((PassManApp) getApplication()).getBiometricManager();

        setupViews();
        observeViewModel();
        animateEntrance();
    }

    private void setupViews() {
        // Login button
        binding.btnLogin.setOnClickListener(v -> {
            String password = binding.etMasterPassword.getText().toString();
            viewModel.login(password);
        });

        // Create vault button
        binding.btnCreateVault.setOnClickListener(v -> {
            String password = binding.etMasterPassword.getText().toString();
            String confirmPassword = binding.etConfirmPassword.getText().toString();
            viewModel.createVault(password, confirmPassword);
        });

        // Biometric button
        binding.btnBiometric.setOnClickListener(v -> {
            showBiometricPrompt();
        });

        // Password field text watcher for real-time validation
        binding.etMasterPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilMasterPassword.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void observeViewModel() {
        // Auth state
        viewModel.getAuthState().observe(this, state -> {
            switch (state) {
                case LOADING:
                    showLoading(true);
                    break;
                case LOGIN:
                    showLoading(false);
                    showLoginMode();
                    break;
                case CREATE_VAULT:
                    showLoading(false);
                    showCreateVaultMode();
                    break;
                case SUCCESS:
                    showLoading(false);
                    navigateToMain();
                    break;
            }
        });

        // Loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.btnLogin.setEnabled(!isLoading);
            binding.btnCreateVault.setEnabled(!isLoading);
            binding.progressIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        // Error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                binding.tilMasterPassword.setError(error);
                // Also show snackbar for visibility
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColor(R.color.error))
                        .show();
            }
        });
    }

    private void showLoading(boolean show) {
        binding.loadingContainer.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.contentContainer.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showLoginMode() {
        binding.tvTitle.setText(R.string.welcome_title);
        binding.tvSubtitle.setText(R.string.welcome_subtitle);
        binding.btnLogin.setVisibility(View.VISIBLE);
        binding.btnCreateVault.setVisibility(View.GONE);
        binding.tilConfirmPassword.setVisibility(View.GONE);

        // Show biometric button if available
        if (viewModel.canUseBiometric()) {
            binding.btnBiometric.setVisibility(View.VISIBLE);
            binding.dividerOr.setVisibility(View.VISIBLE);
            // Auto-show biometric prompt on login screen
            showBiometricPromptAuto();
        } else {
            binding.btnBiometric.setVisibility(View.GONE);
            binding.dividerOr.setVisibility(View.GONE);
        }
    }

    private void showBiometricPromptAuto() {
        // Small delay to let the UI render first
        binding.getRoot().postDelayed(() -> {
            if (!isFinishing() && viewModel.canUseBiometric()) {
                showBiometricPrompt();
            }
        }, 300);
    }

    private void showCreateVaultMode() {
        binding.tvTitle.setText(R.string.create_vault);
        binding.tvSubtitle.setText("Create a master password to secure your vault");
        binding.btnLogin.setVisibility(View.GONE);
        binding.btnCreateVault.setVisibility(View.VISIBLE);
        binding.tilConfirmPassword.setVisibility(View.VISIBLE);
        binding.btnBiometric.setVisibility(View.GONE);
    }

    private void showBiometricPrompt() {
        if (!biometricManager.isBiometricAvailable()) {
            Snackbar.make(binding.getRoot(), 
                    biometricManager.getBiometricStatusMessage(), 
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        biometricManager.authenticate(
                this,
                getString(R.string.biometric_prompt_title),
                getString(R.string.biometric_prompt_subtitle),
                getString(R.string.biometric_prompt_negative),
                new BiometricManager.BiometricAuthCallback() {
                    @Override
                    public void onSuccess() {
                        viewModel.authenticateWithBiometric();
                    }

                    @Override
                    public void onFailed() {
                        Snackbar.make(binding.getRoot(), 
                                "Biometric not recognized", 
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int errorCode, String errorMessage) {
                        Snackbar.make(binding.getRoot(), 
                                errorMessage, 
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUsePassword() {
                        // User chose to use password, do nothing
                    }
                }
        );
    }

    private void animateEntrance() {
        binding.ivLogo.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_in_scale));
        binding.tvTitle.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_up));
        binding.cardAuth.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_up_delayed));
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
