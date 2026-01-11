package com.passman.android.ui.credential;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.snackbar.Snackbar;
import com.passman.android.R;
import com.passman.android.databinding.ActivityAddEditCredentialBinding;
import com.passman.android.ui.generator.PasswordGeneratorActivity;

/**
 * Activity for adding or editing credentials
 */
public class AddEditCredentialActivity extends AppCompatActivity {

    private static final int REQUEST_GENERATE_PASSWORD = 100;
    
    // Categories for the dropdown
    private static final String[] CATEGORIES = {
            "Login",
            "Bank",
            "Credit Card",
            "Social Media",
            "Email",
            "Shopping",
            "Entertainment",
            "Gaming",
            "Education",
            "Work",
            "Other"
    };

    private ActivityAddEditCredentialBinding binding;
    private CredentialViewModel viewModel;
    private long credentialId = -1;
    private boolean isPasswordVisible = false;
    private ArrayAdapter<String> categoryAdapter;
    private String selectedCategory = CATEGORIES[0];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddEditCredentialBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        credentialId = getIntent().getLongExtra("credential_id", -1);
        viewModel = new ViewModelProvider(this).get(CredentialViewModel.class);

        setupToolbar();
        setupViews();
        observeViewModel();

        if (credentialId > 0) {
            viewModel.loadCredential(credentialId);
        } else {
            // Check for imported data from QR code
            handleImportedData();
        }
    }

    private void handleImportedData() {
        Intent intent = getIntent();
        if (intent.hasExtra("import_title")) {
            binding.etTitle.setText(intent.getStringExtra("import_title"));
            binding.etUsername.setText(intent.getStringExtra("import_username"));
            binding.etEmail.setText(intent.getStringExtra("import_email"));
            binding.etPassword.setText(intent.getStringExtra("import_password"));
            binding.etUrl.setText(intent.getStringExtra("import_url"));
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(credentialId > 0 ? "Edit Credential" : "Add Credential");
        }
    }

    private void setupViews() {
        // Toggle password visibility
        binding.tilPassword.setEndIconOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility();
        });

        // Generate password button
        binding.btnGeneratePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, PasswordGeneratorActivity.class);
            intent.putExtra("select_mode", true);
            startActivityForResult(intent, REQUEST_GENERATE_PASSWORD);
        });

        // Password strength indicator
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Username change listener for personal info detection
        binding.etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setCurrentUsername(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Email change listener for personal info detection
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setCurrentEmail(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Save button
        binding.btnSave.setOnClickListener(v -> saveCredential());

        // Category dropdown
        setupCategoryDropdown();
    }

    private void setupCategoryDropdown() {
        categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                CATEGORIES
        );
        binding.actvCategory.setAdapter(categoryAdapter);
        binding.actvCategory.setText(CATEGORIES[0], false);
        selectedCategory = CATEGORIES[0];

        binding.actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = CATEGORIES[position];
        });
    }

    private String getSelectedCategory() {
        return selectedCategory;
    }

    private void observeViewModel() {
        viewModel.getCredential().observe(this, credential -> {
            if (credential == null) return;

            binding.etTitle.setText(credential.getTitle());
            binding.etUsername.setText(credential.getUsername());
            binding.etEmail.setText(credential.getEmail());
            binding.etUrl.setText(credential.getUrl());
            binding.etNotes.setText(credential.getNotes());
            binding.etTags.setText(credential.getTags());

            // Set category in dropdown
            String category = credential.getCategory();
            if (category != null && !category.isEmpty()) {
                binding.actvCategory.setText(category, false);
                selectedCategory = category;
            }
            
            // Set favorite switch
            binding.switchFavorite.setChecked(credential.isFavorite());
        });

        viewModel.getDecryptedPassword().observe(this, password -> {
            if (password != null && !password.isEmpty()) {
                binding.etPassword.setText(password);
            }
        });

        viewModel.getPasswordStrength().observe(this, strength -> {
            binding.progressBar.setProgress(strength);
            updateStrengthUI(strength);
        });

        viewModel.getPasswordWarning().observe(this, warning -> {
            if (warning != null && !warning.isEmpty()) {
                binding.tvPasswordWarning.setText(warning);
                binding.tvPasswordWarning.setVisibility(View.VISIBLE);
            } else {
                binding.tvPasswordWarning.setVisibility(View.GONE);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.btnSave.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColor(R.color.error))
                        .show();
            }
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (success != null && success) {
                Snackbar.make(binding.getRoot(), R.string.credential_saved, Snackbar.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updatePasswordVisibility() {
        if (isPasswordVisible) {
            binding.etPassword.setInputType(
                    android.text.InputType.TYPE_CLASS_TEXT | 
                    android.text.InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            binding.tilPassword.setEndIconDrawable(R.drawable.ic_visibility_off);
        } else {
            binding.etPassword.setInputType(
                    android.text.InputType.TYPE_CLASS_TEXT | 
                    android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
            binding.tilPassword.setEndIconDrawable(R.drawable.ic_visibility);
        }
        binding.etPassword.setSelection(binding.etPassword.length());
    }

    private void updateStrengthUI(int strength) {
        int color;
        String label;

        if (strength < 30) {
            color = getColor(R.color.strength_weak);
            label = getString(R.string.strength_weak);
        } else if (strength < 50) {
            color = getColor(R.color.strength_fair);
            label = getString(R.string.strength_fair);
        } else if (strength < 70) {
            color = getColor(R.color.strength_good);
            label = getString(R.string.strength_good);
        } else if (strength < 85) {
            color = getColor(R.color.strength_strong);
            label = getString(R.string.strength_strong);
        } else {
            color = getColor(R.color.strength_very_strong);
            label = getString(R.string.strength_very_strong);
        }

        binding.progressBar.setIndicatorColor(color);
        binding.tvStrengthLabel.setText(label);
        binding.tvStrengthLabel.setTextColor(color);
    }

    private void saveCredential() {
        String title = binding.etTitle.getText().toString();
        String username = binding.etUsername.getText().toString();
        String email = binding.etEmail.getText().toString();
        String url = binding.etUrl.getText().toString();
        String password = binding.etPassword.getText().toString();
        String notes = binding.etNotes.getText().toString();
        String tags = binding.etTags.getText().toString();
        String category = getSelectedCategory();

        viewModel.saveCredential(title, username, email, url, password, notes, tags, category);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GENERATE_PASSWORD && resultCode == RESULT_OK) {
            String password = data.getStringExtra("generated_password");
            if (password != null) {
                binding.etPassword.setText(password);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (credentialId > 0) {
            getMenuInflater().inflate(R.menu.menu_add_edit_credential, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_delete) {
            viewModel.deleteCredential();
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
