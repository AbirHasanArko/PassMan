package com.passman.android.ui.vault;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.passman.android.databinding.ActivityFileVaultBinding;
import com.passman.core.model.FileVault;

/**
 * Activity for managing encrypted file vaults
 */
public class FileVaultActivity extends AppCompatActivity {

    private ActivityFileVaultBinding binding;
    private FileVaultViewModel viewModel;
    private FileVaultAdapter vaultAdapter;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        // Not used in list view - files are added from vault details
                        Toast.makeText(this, "Please open a vault first", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFileVaultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("File Vault");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(FileVaultViewModel.class);

        // Setup RecyclerView
        vaultAdapter = new FileVaultAdapter(vault -> openVault(vault));
        binding.rvFileVaults.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvFileVaults.setAdapter(vaultAdapter);

        // Observe data
        viewModel.getFileVaults().observe(this, vaults -> {
            if (vaults.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvFileVaults.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvFileVaults.setVisibility(View.VISIBLE);
                vaultAdapter.submitList(vaults);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        // Setup buttons
        binding.fabCreateVault.setOnClickListener(v -> showCreateVaultDialog());
    }

    private void showCreateVaultDialog() {
        final FileVault.VaultType[] vaultTypes = FileVault.VaultType.values();
        String[] typeNames = {"🖼️ Images", "📄 PDFs", "📁 Documents", "📦 Other Files", "🔐 Custom"};

        new MaterialAlertDialogBuilder(this)
                .setTitle("Select Vault Type")
                .setItems(typeNames, (dialog, which) -> {
                    FileVault.VaultType selectedType = vaultTypes[which];
                    showVaultNameDialog(selectedType);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showVaultNameDialog(FileVault.VaultType vaultType) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Enter vault name");
        
        // Suggest a default name based on type
        String defaultName = getDefaultVaultName(vaultType);
        input.setText(defaultName);
        input.selectAll();

        new MaterialAlertDialogBuilder(this)
                .setTitle("Name Your Vault")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String vaultName = input.getText().toString().trim();
                    if (vaultName.isEmpty()) {
                        vaultName = defaultName;
                    }
                    String emoji = FileVaultViewModel.getEmojiForType(vaultType);
                    viewModel.createNewVault(vaultName, vaultType, emoji);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getDefaultVaultName(FileVault.VaultType type) {
        switch (type) {
            case IMAGES: return "My Images";
            case PDFS: return "My PDFs";
            case DOCUMENTS: return "My Documents";
            case OTHERS: return "My Files";
            case CUSTOM: return "My Vault";
            default: return "New Vault";
        }
    }

    private void openVault(FileVault vault) {
        Intent intent = new Intent(this, VaultDetailsActivity.class);
        intent.putExtra("vault_id", vault.getId());
        intent.putExtra("vault_name", vault.getVaultName());
        intent.putExtra("vault_type", vault.getVaultType().name());
        startActivity(intent);
    }

    private void pickFileForEncryption() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(Intent.createChooser(intent, "Select file to encrypt"));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
