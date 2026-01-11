package com.passman.android.ui.credential;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.passman.android.R;
import com.passman.android.databinding.ActivityCredentialDetailBinding;
import com.passman.android.util.QRCodeGenerator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Activity to display credential details
 */
public class CredentialDetailActivity extends AppCompatActivity {

    private ActivityCredentialDetailBinding binding;
    private CredentialViewModel viewModel;
    private long credentialId;
    private boolean isPasswordVisible = false;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCredentialDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        credentialId = getIntent().getLongExtra("credential_id", -1);
        if (credentialId == -1) {
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(CredentialViewModel.class);

        setupToolbar();
        setupViews();
        observeViewModel();

        // Load credential
        viewModel.loadCredential(credentialId);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }

    private void setupViews() {
        // Toggle password visibility
        binding.btnTogglePassword.setOnClickListener(v -> {
            isPasswordVisible = !isPasswordVisible;
            updatePasswordVisibility();
        });

        // Copy buttons
        binding.btnCopyUsername.setOnClickListener(v -> {
            copyToClipboard("Username", binding.tvUsername.getText().toString());
        });

        binding.btnCopyEmail.setOnClickListener(v -> {
            copyToClipboard("Email", binding.tvEmail.getText().toString());
        });

        binding.btnCopyPassword.setOnClickListener(v -> {
            String password = viewModel.getDecryptedPassword().getValue();
            if (password != null) {
                copyToClipboard("Password", password);
            }
        });

        binding.btnCopyUrl.setOnClickListener(v -> {
            copyToClipboard("URL", binding.tvUrl.getText().toString());
        });

        // Edit button
        binding.fabEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditCredentialActivity.class);
            intent.putExtra("credential_id", credentialId);
            startActivity(intent);
        });

        // Favorite toggle
        binding.btnFavorite.setOnClickListener(v -> {
            viewModel.toggleFavorite();
        });
    }

    private void observeViewModel() {
        viewModel.getCredential().observe(this, credential -> {
            if (credential == null) return;

            // Title
            binding.tvTitle.setText(credential.getTitle());

            // Username
            if (credential.getUsername() != null && !credential.getUsername().isEmpty()) {
                binding.cardUsername.setVisibility(View.VISIBLE);
                binding.tvUsername.setText(credential.getUsername());
            } else {
                binding.cardUsername.setVisibility(View.GONE);
            }

            // Email
            if (credential.getEmail() != null && !credential.getEmail().isEmpty()) {
                binding.cardEmail.setVisibility(View.VISIBLE);
                binding.tvEmail.setText(credential.getEmail());
            } else {
                binding.cardEmail.setVisibility(View.GONE);
            }

            // URL
            if (credential.getUrl() != null && !credential.getUrl().isEmpty()) {
                binding.cardUrl.setVisibility(View.VISIBLE);
                binding.tvUrl.setText(credential.getUrl());
            } else {
                binding.cardUrl.setVisibility(View.GONE);
            }

            // Notes
            if (credential.getNotes() != null && !credential.getNotes().isEmpty()) {
                binding.cardNotes.setVisibility(View.VISIBLE);
                binding.tvNotes.setText(credential.getNotes());
            } else {
                binding.cardNotes.setVisibility(View.GONE);
            }

            // Favorite icon
            updateFavoriteIcon(credential.isFavorite());

            // Password strength
            int strength = credential.getPasswordStrengthScore();
            binding.progressStrength.setProgress(strength);
            updateStrengthColor(strength);

            // Dates
            if (credential.getCreatedAt() > 0) {
                binding.tvCreatedAt.setText("Created: " + 
                        dateFormat.format(new Date(credential.getCreatedAt())));
            }
            if (credential.getLastModified() > 0) {
                binding.tvLastModified.setText("Modified: " + 
                        dateFormat.format(new Date(credential.getLastModified())));
            }
            if (credential.getPasswordChangedAt() > 0) {
                long age = (System.currentTimeMillis() - credential.getPasswordChangedAt()) 
                        / (1000 * 60 * 60 * 24);
                binding.tvPasswordAge.setText("Password age: " + age + " days");
                if (age > 90) {
                    binding.tvPasswordAge.setTextColor(getColor(R.color.warning));
                }
            }
        });

        viewModel.getDecryptedPassword().observe(this, password -> {
            if (password != null) {
                updatePasswordVisibility();
            }
        });

        viewModel.getPasswordStrength().observe(this, strength -> {
            binding.progressStrength.setProgress(strength);
            updateStrengthColor(strength);
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            binding.contentContainer.setVisibility(isLoading ? View.GONE : View.VISIBLE);
        });

        viewModel.getSaveSuccess().observe(this, success -> {
            if (success != null && success) {
                finish();
            }
        });
    }

    private void updatePasswordVisibility() {
        String password = viewModel.getDecryptedPassword().getValue();
        if (password == null) return;

        if (isPasswordVisible) {
            binding.tvPassword.setText(password);
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility_off);
        } else {
            binding.tvPassword.setText("••••••••••••");
            binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility);
        }
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
            binding.btnFavorite.setColorFilter(getColor(R.color.warning));
        } else {
            binding.btnFavorite.setImageResource(R.drawable.ic_favorite_outline);
            binding.btnFavorite.setColorFilter(getColor(R.color.icon_secondary_light));
        }
    }

    private void updateStrengthColor(int score) {
        int color;
        String label;
        if (score < 30) {
            color = getColor(R.color.strength_weak);
            label = "Weak";
        } else if (score < 50) {
            color = getColor(R.color.strength_fair);
            label = "Fair";
        } else if (score < 70) {
            color = getColor(R.color.strength_good);
            label = "Good";
        } else if (score < 85) {
            color = getColor(R.color.strength_strong);
            label = "Strong";
        } else {
            color = getColor(R.color.strength_very_strong);
            label = "Very Strong";
        }
        binding.progressStrength.setIndicatorColor(color);
        binding.tvStrengthLabel.setText(label);
        binding.tvStrengthLabel.setTextColor(color);
    }

    private void copyToClipboard(String label, String text) {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);

        Snackbar.make(binding.getRoot(), R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT).show();

        // Auto-clear password from clipboard after 30 seconds
        if (label.equals("Password")) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
            }, 30000);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_credential_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_delete) {
            showDeleteConfirmation();
            return true;
        } else if (id == R.id.action_share_qr) {
            showQRCodeDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showQRCodeDialog() {
        // Get credential data
        var credential = viewModel.getCredential().getValue();
        String password = viewModel.getDecryptedPassword().getValue();
        
        if (credential == null) {
            Snackbar.make(binding.getRoot(), "Could not load credential", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Generate QR code
        Bitmap qrBitmap = QRCodeGenerator.generateCredentialQR(
                credential.getTitle(),
                credential.getUsername(),
                credential.getEmail(),
                password,
                credential.getUrl()
        );

        if (qrBitmap == null) {
            Snackbar.make(binding.getRoot(), "Failed to generate QR code", Snackbar.LENGTH_SHORT).show();
            return;
        }

        // Create dialog with QR code
        ImageView imageView = new ImageView(this);
        int padding = (int) (24 * getResources().getDisplayMetrics().density);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setImageBitmap(qrBitmap);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);

        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle("Share via QR Code")
                .setMessage("Scan this QR code with another PassMan device to transfer the credential securely.")
                .setView(imageView)
                .setPositiveButton("Done", null)
                .setNeutralButton("Share Image", (dialog, which) -> {
                    shareQRCodeImage(qrBitmap, credential.getTitle());
                })
                .show();
    }

    private void shareQRCodeImage(Bitmap qrBitmap, String title) {
        try {
            // Save bitmap to cache directory
            java.io.File cachePath = new java.io.File(getCacheDir(), "images");
            cachePath.mkdirs();
            java.io.File file = new java.io.File(cachePath, "qr_" + title.replaceAll("[^a-zA-Z0-9]", "_") + ".png");
            
            java.io.FileOutputStream stream = new java.io.FileOutputStream(file);
            qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            // Get URI using FileProvider
            android.net.Uri contentUri = androidx.core.content.FileProvider.getUriForFile(
                    this, 
                    getPackageName() + ".fileprovider", 
                    file
            );

            // Create share intent
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Share QR Code"));
            
        } catch (Exception e) {
            Snackbar.make(binding.getRoot(), "Failed to share QR code", Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmation() {
        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteCredential();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload in case data was edited
        viewModel.loadCredential(credentialId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
