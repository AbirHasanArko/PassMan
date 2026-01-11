package com.passman.android.ui.settings;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.FrameLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.passman.android.PassManApp;
import com.passman.android.R;
import com.passman.android.data.repository.CredentialRepository;
import com.passman.android.databinding.ActivitySettingsBinding;
import com.passman.android.notification.NotificationHelper;
import com.passman.android.notification.NotificationScheduler;
import com.passman.android.security.BiometricManager;
import com.passman.android.security.CryptoManager;
import com.passman.android.security.SessionManager;
import com.passman.android.ui.auth.AuthActivity;

import javax.crypto.SecretKey;

/**
 * Settings Activity - Complete implementation
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "passman_settings";
    private static final String KEY_AUTO_LOCK = "auto_lock_timeout";
    private static final String KEY_CLIPBOARD_TIMEOUT = "clipboard_timeout";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_NOTIFICATIONS_ENABLED = "notifications_enabled";
    private static final String KEY_SECURITY_ALERTS = "security_alerts_enabled";
    private static final String KEY_PASSWORD_REMINDERS = "password_reminders_enabled";

    private ActivitySettingsBinding binding;
    private SessionManager sessionManager;
    private BiometricManager biometricManager;
    private SharedPreferences settingsPrefs;
    private CredentialRepository credentialRepository;
    private NotificationScheduler notificationScheduler;
    private NotificationHelper notificationHelper;
    
    // Permission launcher for notification permission (Android 13+)
    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    enableNotifications();
                } else {
                    binding.switchNotifications.setChecked(false);
                    Snackbar.make(binding.getRoot(), 
                            "Notification permission is required for alerts", 
                            Snackbar.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        PassManApp app = (PassManApp) getApplication();
        sessionManager = app.getSessionManager();
        biometricManager = app.getBiometricManager();
        credentialRepository = app.getCredentialRepository();
        notificationScheduler = app.getNotificationScheduler();
        notificationHelper = app.getNotificationHelper();
        settingsPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        setupToolbar();
        setupViews();
        setupClickListeners();
        loadSettings();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }
    }

    private void setupViews() {
        // Biometric switch
        binding.switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableBiometric();
            } else {
                disableBiometric();
            }
        });

        // Check biometric availability
        if (!biometricManager.isBiometricAvailable()) {
            binding.switchBiometric.setEnabled(false);
            binding.layoutBiometric.setAlpha(0.5f);
        }
        
        // Notification switches
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                requestNotificationPermission();
            } else {
                disableNotifications();
            }
        });
        
        binding.switchSecurityAlerts.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean(KEY_SECURITY_ALERTS, isChecked).apply();
            updateNotificationSchedule();
        });
        
        binding.switchPasswordReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            settingsPrefs.edit().putBoolean(KEY_PASSWORD_REMINDERS, isChecked).apply();
            updateNotificationSchedule();
        });

        // Set app version
        try {
            String versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            binding.tvAppVersion.setText("Version " + versionName);
        } catch (Exception e) {
            binding.tvAppVersion.setText("Version 1.0.0");
        }
    }

    private void setupClickListeners() {
        // Auto-lock setting
        binding.layoutAutoLock.setOnClickListener(v -> showAutoLockDialog());

        // Clipboard timeout setting
        binding.layoutClipboard.setOnClickListener(v -> showClipboardDialog());

        // Change master password
        binding.layoutChangeMasterPassword.setOnClickListener(v -> showChangeMasterPasswordDialog());

        // Dark mode setting
        binding.layoutDarkMode.setOnClickListener(v -> showThemeDialog());

        // Export data
        binding.layoutExport.setOnClickListener(v -> exportData());

        // Import data
        binding.layoutImport.setOnClickListener(v -> importData());

        // Privacy policy
        binding.layoutPrivacy.setOnClickListener(v -> openUrl("https://passman.example.com/privacy"));

        // Terms of service
        binding.layoutTerms.setOnClickListener(v -> openUrl("https://passman.example.com/terms"));

        // Delete all data
        binding.layoutDeleteAll.setOnClickListener(v -> showDeleteAllConfirmation());
        
        // Test notification
        binding.layoutTestNotification.setOnClickListener(v -> sendTestNotification());

        // About section social links
        binding.layoutGithub.setOnClickListener(v -> openUrl("https://github.com/AbirHasanArko"));
        binding.layoutLinkedin.setOnClickListener(v -> openUrl("https://www.linkedin.com/in/abirhasanarko/"));
        binding.layoutTwitter.setOnClickListener(v -> openUrl("https://x.com/AbirHasanArko"));
    }

    private void loadSettings() {
        // Biometric
        binding.switchBiometric.setChecked(sessionManager.hasBiometricKey());

        // Auto-lock value
        int autoLockValue = settingsPrefs.getInt(KEY_AUTO_LOCK, 5);
        binding.tvAutoLockValue.setText(getAutoLockText(autoLockValue));

        // Clipboard value
        int clipboardValue = settingsPrefs.getInt(KEY_CLIPBOARD_TIMEOUT, 30);
        binding.tvClipboardValue.setText(getClipboardText(clipboardValue));

        // Dark mode value
        int darkModeValue = settingsPrefs.getInt(KEY_DARK_MODE, 0);
        binding.tvDarkModeValue.setText(getDarkModeText(darkModeValue));
        
        // Notification settings
        boolean notificationsEnabled = settingsPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        boolean securityAlertsEnabled = settingsPrefs.getBoolean(KEY_SECURITY_ALERTS, true);
        boolean passwordRemindersEnabled = settingsPrefs.getBoolean(KEY_PASSWORD_REMINDERS, true);
        
        binding.switchNotifications.setChecked(notificationsEnabled);
        binding.switchSecurityAlerts.setChecked(securityAlertsEnabled);
        binding.switchPasswordReminders.setChecked(passwordRemindersEnabled);
        
        // Enable/disable sub-switches based on main notification switch
        updateNotificationSubSwitches(notificationsEnabled);

        // Version
        try {
            String version = getPackageManager()
                    .getPackageInfo(getPackageName(), 0).versionName;
            binding.tvVersion.setText("Version " + version);
        } catch (Exception e) {
            binding.tvVersion.setText("Version 1.0.0");
        }
    }
    
    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            } else {
                enableNotifications();
            }
        } else {
            enableNotifications();
        }
    }
    
    private void enableNotifications() {
        settingsPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, true).apply();
        updateNotificationSubSwitches(true);
        notificationScheduler.scheduleSecurityChecks();
        Snackbar.make(binding.getRoot(), "Notifications enabled", Snackbar.LENGTH_SHORT).show();
    }
    
    private void disableNotifications() {
        settingsPrefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, false).apply();
        updateNotificationSubSwitches(false);
        notificationScheduler.cancelSecurityChecks();
        Snackbar.make(binding.getRoot(), "Notifications disabled", Snackbar.LENGTH_SHORT).show();
    }
    
    private void updateNotificationSubSwitches(boolean enabled) {
        binding.switchSecurityAlerts.setEnabled(enabled);
        binding.switchPasswordReminders.setEnabled(enabled);
        binding.layoutSecurityAlerts.setAlpha(enabled ? 1.0f : 0.5f);
        binding.layoutPasswordReminders.setAlpha(enabled ? 1.0f : 0.5f);
    }
    
    private void updateNotificationSchedule() {
        boolean notificationsEnabled = settingsPrefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true);
        if (notificationsEnabled) {
            notificationScheduler.scheduleSecurityChecks();
        }
    }
    
    private void sendTestNotification() {
        if (notificationHelper.hasNotificationPermission()) {
            notificationHelper.showTestNotification();
            Snackbar.make(binding.getRoot(), "Test notification sent!", Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(binding.getRoot(), "Please enable notifications first", Snackbar.LENGTH_SHORT).show();
        }
    }

    private String getAutoLockText(int minutes) {
        switch (minutes) {
            case 0: return getString(R.string.auto_lock_immediately);
            case 1: return getString(R.string.auto_lock_1_min);
            case 5: return getString(R.string.auto_lock_5_min);
            case 15: return getString(R.string.auto_lock_15_min);
            default: return getString(R.string.auto_lock_5_min);
        }
    }

    private String getClipboardText(int seconds) {
        switch (seconds) {
            case 30: return getString(R.string.clipboard_clear_30s);
            case 60: return getString(R.string.clipboard_clear_1_min);
            case -1: return getString(R.string.clipboard_clear_never);
            default: return getString(R.string.clipboard_clear_30s);
        }
    }

    private String getDarkModeText(int mode) {
        switch (mode) {
            case 0: return getString(R.string.dark_mode_system);
            case 1: return getString(R.string.dark_mode_light);
            case 2: return getString(R.string.dark_mode_dark);
            default: return getString(R.string.dark_mode_system);
        }
    }

    private void enableBiometric() {
        if (!biometricManager.isBiometricAvailable()) {
            binding.switchBiometric.setChecked(false);
            Snackbar.make(binding.getRoot(), 
                    biometricManager.getBiometricStatusMessage(), 
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        biometricManager.authenticate(
                this,
                "Enable Biometric",
                "Verify your identity to enable biometric unlock",
                "Cancel",
                new BiometricManager.BiometricAuthCallback() {
                    @Override
                    public void onSuccess() {
                        // Store master key for biometric
                        byte[] keyBytes = sessionManager.getMasterKey().getEncoded();
                        sessionManager.storeMasterKeyForBiometric(keyBytes);
                        Snackbar.make(binding.getRoot(), 
                                "Biometric unlock enabled", 
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailed() {
                        binding.switchBiometric.setChecked(false);
                    }

                    @Override
                    public void onError(int errorCode, String errorMessage) {
                        binding.switchBiometric.setChecked(false);
                        Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onUsePassword() {
                        binding.switchBiometric.setChecked(false);
                    }
                }
        );
    }

    private void disableBiometric() {
        sessionManager.clearBiometricKey();
        Snackbar.make(binding.getRoot(), "Biometric unlock disabled", Snackbar.LENGTH_SHORT).show();
    }

    private void showAutoLockDialog() {
        String[] options = {
                getString(R.string.auto_lock_immediately),
                getString(R.string.auto_lock_1_min),
                getString(R.string.auto_lock_5_min),
                getString(R.string.auto_lock_15_min)
        };
        int[] values = {0, 1, 5, 15};
        int currentValue = settingsPrefs.getInt(KEY_AUTO_LOCK, 5);
        int selectedIndex = 2; // default to 5 min
        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentValue) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle(R.string.auto_lock)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    settingsPrefs.edit().putInt(KEY_AUTO_LOCK, values[which]).apply();
                    binding.tvAutoLockValue.setText(options[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showClipboardDialog() {
        String[] options = {
                getString(R.string.clipboard_clear_30s),
                getString(R.string.clipboard_clear_1_min),
                getString(R.string.clipboard_clear_never)
        };
        int[] values = {30, 60, -1};
        int currentValue = settingsPrefs.getInt(KEY_CLIPBOARD_TIMEOUT, 30);
        int selectedIndex = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == currentValue) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle(R.string.clipboard_clear)
                .setSingleChoiceItems(options, selectedIndex, (dialog, which) -> {
                    settingsPrefs.edit().putInt(KEY_CLIPBOARD_TIMEOUT, values[which]).apply();
                    binding.tvClipboardValue.setText(options[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showThemeDialog() {
        String[] options = {
                getString(R.string.dark_mode_system),
                getString(R.string.dark_mode_light),
                getString(R.string.dark_mode_dark)
        };
        int currentValue = settingsPrefs.getInt(KEY_DARK_MODE, 0);

        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle(R.string.dark_mode)
                .setSingleChoiceItems(options, currentValue, (dialog, which) -> {
                    settingsPrefs.edit().putInt(KEY_DARK_MODE, which).apply();
                    binding.tvDarkModeValue.setText(options[which]);
                    switch (which) {
                        case 0:
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            break;
                        case 1:
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_NO);
                            break;
                        case 2:
                            AppCompatDelegate.setDefaultNightMode(
                                    AppCompatDelegate.MODE_NIGHT_YES);
                            break;
                    }
                    dialog.dismiss();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showChangeMasterPasswordDialog() {
        // Create dialog with password inputs
        FrameLayout container = new FrameLayout(this);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, 0);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        
        TextInputLayout tilCurrent = new TextInputLayout(this, null, 
                com.google.android.material.R.attr.textInputOutlinedStyle);
        tilCurrent.setHint("Current Password");
        TextInputEditText etCurrent = new TextInputEditText(tilCurrent.getContext());
        etCurrent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilCurrent.addView(etCurrent);
        
        TextInputLayout tilNew = new TextInputLayout(this, null,
                com.google.android.material.R.attr.textInputOutlinedStyle);
        tilNew.setHint("New Password");
        TextInputEditText etNew = new TextInputEditText(tilNew.getContext());
        etNew.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilNew.addView(etNew);

        TextInputLayout tilConfirm = new TextInputLayout(this, null,
                com.google.android.material.R.attr.textInputOutlinedStyle);
        tilConfirm.setHint("Confirm New Password");
        TextInputEditText etConfirm = new TextInputEditText(tilConfirm.getContext());
        etConfirm.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        tilConfirm.addView(etConfirm);

        layout.addView(tilCurrent);
        layout.addView(tilNew);
        layout.addView(tilConfirm);
        container.addView(layout);

        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle(R.string.change_master_password)
                .setView(container)
                .setPositiveButton("Change", (dialog, which) -> {
                    String current = etCurrent.getText().toString();
                    String newPass = etNew.getText().toString();
                    String confirm = etConfirm.getText().toString();

                    if (newPass.length() < 8) {
                        Snackbar.make(binding.getRoot(), R.string.password_min_length, 
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if (!newPass.equals(confirm)) {
                        Snackbar.make(binding.getRoot(), R.string.passwords_dont_match, 
                                Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    // TODO: Verify current password and update
                    Snackbar.make(binding.getRoot(), "Password changed successfully", 
                            Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void exportData() {
        Snackbar.make(binding.getRoot(), "Export feature coming soon", Snackbar.LENGTH_SHORT).show();
    }

    private void importData() {
        Snackbar.make(binding.getRoot(), "Import feature coming soon", Snackbar.LENGTH_SHORT).show();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), "Could not open link", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showDeleteAllConfirmation() {
        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle(R.string.delete_all_data)
                .setMessage("Are you sure you want to delete all your data? This action cannot be undone.")
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    // Show second confirmation
                    new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                            .setTitle("Final Confirmation")
                            .setMessage("Type DELETE to confirm permanent deletion of all data.")
                            .setView(createDeleteConfirmInput())
                            .setPositiveButton("Delete Forever", (d2, w2) -> deleteAllData())
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private EditText createDeleteConfirmInput() {
        EditText input = new EditText(this);
        input.setHint("Type DELETE");
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        input.setPadding(padding, padding, padding, padding);
        return input;
    }

    private void deleteAllData() {
        // Delete all credentials
        credentialRepository.deleteAll(new CredentialRepository.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Clear session and go to login
                sessionManager.clearSession();
                settingsPrefs.edit().clear().apply();
                
                Snackbar.make(binding.getRoot(), "All data deleted", Snackbar.LENGTH_SHORT).show();
                
                Intent intent = new Intent(SettingsActivity.this, AuthActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }

            @Override
            public void onError(Exception e) {
                Snackbar.make(binding.getRoot(), "Failed to delete data", Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void lockVault() {
        sessionManager.lockVault();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showAboutDialog() {
        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle("About PassMan")
                .setMessage("PassMan is a secure password manager that helps you " +
                        "store and manage your passwords safely.\n\n" +
                        "🔒 AES-256 Encryption\n" +
                        "🔑 PBKDF2 Key Derivation\n" +
                        "📱 Biometric Authentication\n" +
                        "☁️ Cloud Backup Support\n\n" +
                        "Made with ❤️ for security enthusiasts.")
                .setPositiveButton("Close", null)
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
