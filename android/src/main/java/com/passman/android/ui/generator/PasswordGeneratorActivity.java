package com.passman.android.ui.generator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import com.passman.android.R;
import com.passman.android.databinding.ActivityPasswordGeneratorBinding;

/**
 * Password Generator Activity
 */
public class PasswordGeneratorActivity extends AppCompatActivity {

    private ActivityPasswordGeneratorBinding binding;
    private PasswordGeneratorViewModel viewModel;
    private boolean selectMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPasswordGeneratorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        selectMode = getIntent().getBooleanExtra("select_mode", false);
        viewModel = new ViewModelProvider(this).get(PasswordGeneratorViewModel.class);

        setupToolbar();
        setupViews();
        observeViewModel();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.password_generator);
        }
    }

    private void setupViews() {
        // Regenerate button
        binding.btnRegenerate.setOnClickListener(v -> {
            viewModel.generate();
            animatePasswordChange();
        });

        // Copy button
        binding.btnCopy.setOnClickListener(v -> {
            String password = viewModel.getCurrentPassword();
            if (password != null) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("password", password));
                Snackbar.make(binding.getRoot(), R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();
            }
        });

        // Use password button (only in select mode)
        if (selectMode) {
            binding.btnUsePassword.setVisibility(android.view.View.VISIBLE);
            binding.btnUsePassword.setOnClickListener(v -> {
                Intent result = new Intent();
                result.putExtra("generated_password", viewModel.getCurrentPassword());
                setResult(RESULT_OK, result);
                finish();
            });
        } else {
            binding.btnUsePassword.setVisibility(android.view.View.GONE);
        }

        // Length slider
        binding.sliderLength.addOnChangeListener((slider, value, fromUser) -> {
            if (fromUser) {
                viewModel.setLength((int) value);
            }
        });

        // Character type switches
        binding.switchUppercase.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setIncludeUppercase(isChecked);
        });

        binding.switchLowercase.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setIncludeLowercase(isChecked);
        });

        binding.switchNumbers.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setIncludeNumbers(isChecked);
        });

        binding.switchSymbols.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setIncludeSymbols(isChecked);
        });

        binding.switchAmbiguous.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setExcludeAmbiguous(isChecked);
        });

        // TODO: Preset chip buttons not yet implemented in layout
        // binding.chipEasy, binding.chipSecure, binding.chipPin, binding.chipMemorable
    }

    private void observeViewModel() {
        viewModel.getGeneratedPassword().observe(this, password -> {
            binding.tvGeneratedPassword.setText(password);
        });

        viewModel.getPasswordStrength().observe(this, strength -> {
            binding.progressStrength.setProgress(strength);
            updateStrengthUI(strength);
        });

        viewModel.getStrengthLabel().observe(this, label -> {
            binding.tvStrengthLabel.setText(label);
        });

        viewModel.getLength().observe(this, length -> {
            binding.tvLengthValue.setText(String.valueOf(length));
            if (binding.sliderLength.getValue() != length) {
                binding.sliderLength.setValue(length);
            }
        });

        viewModel.getIncludeUppercase().observe(this, checked -> {
            if (binding.switchUppercase.isChecked() != checked) {
                binding.switchUppercase.setChecked(checked);
            }
        });

        viewModel.getIncludeLowercase().observe(this, checked -> {
            if (binding.switchLowercase.isChecked() != checked) {
                binding.switchLowercase.setChecked(checked);
            }
        });

        viewModel.getIncludeNumbers().observe(this, checked -> {
            if (binding.switchNumbers.isChecked() != checked) {
                binding.switchNumbers.setChecked(checked);
            }
        });

        viewModel.getIncludeSymbols().observe(this, checked -> {
            if (binding.switchSymbols.isChecked() != checked) {
                binding.switchSymbols.setChecked(checked);
            }
        });

        viewModel.getExcludeAmbiguous().observe(this, checked -> {
            if (binding.switchAmbiguous.isChecked() != checked) {
                binding.switchAmbiguous.setChecked(checked);
            }
        });

        viewModel.getPasswordWarning().observe(this, warning -> {
            if (warning != null && !warning.isEmpty()) {
                binding.tvPasswordWarning.setText(warning);
                binding.tvPasswordWarning.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.tvPasswordWarning.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void updateUIFromViewModel() {
        Integer length = viewModel.getLength().getValue();
        if (length != null) {
            binding.sliderLength.setValue(length);
            binding.tvLengthValue.setText(String.valueOf(length));
        }

        Boolean uppercase = viewModel.getIncludeUppercase().getValue();
        if (uppercase != null) binding.switchUppercase.setChecked(uppercase);

        Boolean lowercase = viewModel.getIncludeLowercase().getValue();
        if (lowercase != null) binding.switchLowercase.setChecked(lowercase);

        Boolean numbers = viewModel.getIncludeNumbers().getValue();
        if (numbers != null) binding.switchNumbers.setChecked(numbers);

        Boolean symbols = viewModel.getIncludeSymbols().getValue();
        if (symbols != null) binding.switchSymbols.setChecked(symbols);

        Boolean ambiguous = viewModel.getExcludeAmbiguous().getValue();
        if (ambiguous != null) binding.switchAmbiguous.setChecked(ambiguous);
    }

    private void updateStrengthUI(int strength) {
        int color;
        if (strength < 30) {
            color = getColor(R.color.strength_weak);
        } else if (strength < 50) {
            color = getColor(R.color.strength_fair);
        } else if (strength < 70) {
            color = getColor(R.color.strength_good);
        } else if (strength < 85) {
            color = getColor(R.color.strength_strong);
        } else {
            color = getColor(R.color.strength_very_strong);
        }

        binding.progressStrength.setIndicatorColor(color);
        binding.tvStrengthLabel.setTextColor(color);
    }

    private void animatePasswordChange() {
        binding.cardPassword.animate()
                .scaleX(1.02f)
                .scaleY(1.02f)
                .setDuration(100)
                .withEndAction(() -> {
                    binding.cardPassword.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
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
