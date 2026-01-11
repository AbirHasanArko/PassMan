package com.passman.android.ui.vault;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;
import com.passman.android.databinding.ActivityVaultDetailsBinding;
import com.passman.core.model.FileVault;

/**
 * Activity showing details and files within a specific vault
 */
public class VaultDetailsActivity extends AppCompatActivity {

    private ActivityVaultDetailsBinding binding;
    private FileVaultViewModel viewModel;
    private EncryptedFileAdapter fileAdapter;
    private long vaultId;
    private Snackbar loadingSnackbar;

    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        FileVault vault = viewModel.getCurrentVault().getValue();
                        if (vault != null && !isFileTypeAllowed(fileUri, vault.getVaultType())) {
                            Snackbar.make(binding.getRoot(), 
                                    "This vault only accepts " + getVaultTypeDescription(vault.getVaultType()) + " files",
                                    Snackbar.LENGTH_LONG)
                                    .setBackgroundTint(getColor(android.R.color.holo_orange_dark))
                                    .show();
                            return;
                        }
                        viewModel.encryptAndUploadFile(fileUri, vaultId);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityVaultDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get vault info from intent
        vaultId = getIntent().getLongExtra("vault_id", -1);
        String vaultName = getIntent().getStringExtra("vault_name");
        String vaultTypeName = getIntent().getStringExtra("vault_type");

        if (vaultId == -1) {
            Toast.makeText(this, "Invalid vault", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Setup toolbar with vault name
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(vaultName != null ? vaultName : "Vault");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Set initial values from intent
        binding.tvVaultName.setText(vaultName != null ? vaultName : "Vault");
        if (vaultTypeName != null) {
            binding.tvVaultType.setText("Type: " + formatVaultType(vaultTypeName));
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(FileVaultViewModel.class);
        
        // Load the vault data
        viewModel.setCurrentVault(vaultId);

        // Setup RecyclerView
        fileAdapter = new EncryptedFileAdapter(
                file -> downloadAndDecryptFile(file),
                file -> confirmDeleteFile(file)
        );
        binding.rvEncryptedFiles.setLayoutManager(new LinearLayoutManager(this));
        binding.rvEncryptedFiles.setAdapter(fileAdapter);

        // Observe vault details (updates from database)
        viewModel.getCurrentVault().observe(this, vault -> {
            if (vault != null) {
                binding.tvVaultName.setText(vault.getVaultName());
                binding.tvVaultType.setText("Type: " + formatVaultType(vault.getVaultType().name()));
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(vault.getVaultName());
                }
            }
        });

        // Observe encrypted files for THIS vault
        viewModel.getEncryptedFiles().observe(this, files -> {
            if (files == null || files.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvEncryptedFiles.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvEncryptedFiles.setVisibility(View.VISIBLE);
                fileAdapter.submitList(files);
            }
        });

        // Observe loading state
        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.fabAddFile.setEnabled(!isLoading);
            if (isLoading) {
                loadingSnackbar = Snackbar.make(binding.getRoot(), "Processing...", Snackbar.LENGTH_INDEFINITE);
                loadingSnackbar.show();
            } else {
                if (loadingSnackbar != null && loadingSnackbar.isShown()) {
                    loadingSnackbar.dismiss();
                    loadingSnackbar = null;
                }
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG)
                        .setBackgroundTint(getColor(android.R.color.holo_red_dark))
                        .show();
            }
        });

        // Observe success messages
        viewModel.getSuccessMessage().observe(this, message -> {
            if (message != null && !message.isEmpty()) {
                Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT)
                        .setBackgroundTint(getColor(android.R.color.holo_green_dark))
                        .show();
            }
        });

        // Setup buttons
        binding.fabAddFile.setOnClickListener(v -> pickFileForEncryption());
    }

    private String formatVaultType(String typeName) {
        if (typeName == null) return "Unknown";
        switch (typeName.toUpperCase()) {
            case "IMAGES": return "Images";
            case "PDFS": return "PDFs";
            case "DOCUMENTS": return "Documents";
            case "OTHERS": return "Other Files";
            case "CUSTOM": return "Custom";
            default: return typeName;
        }
    }

    private void downloadAndDecryptFile(com.passman.core.model.EncryptedFile file) {
        viewModel.decryptAndSaveToDownloads(file);
    }

    private void confirmDeleteFile(com.passman.core.model.EncryptedFile file) {
        new AlertDialog.Builder(this)
                .setTitle("Delete File")
                .setMessage("Are you sure you want to delete \"" + file.getOriginalFileName() + "\"? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> viewModel.deleteEncryptedFile(file))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void pickFileForEncryption() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        
        // Set MIME type filter based on vault type
        FileVault vault = viewModel.getCurrentVault().getValue();
        if (vault != null) {
            switch (vault.getVaultType()) {
                case IMAGES:
                    intent.setType("image/*");
                    break;
                case PDFS:
                    intent.setType("application/pdf");
                    break;
                case DOCUMENTS:
                    intent.setType("*/*");
                    String[] documentMimeTypes = {
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "text/plain",
                        "text/csv"
                    };
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, documentMimeTypes);
                    break;
                default:
                    intent.setType("*/*");
                    break;
            }
        } else {
            intent.setType("*/*");
        }
        
        filePickerLauncher.launch(Intent.createChooser(intent, "Select file to encrypt"));
    }

    /**
     * Checks if the selected file type is allowed for the given vault type
     */
    private boolean isFileTypeAllowed(Uri fileUri, FileVault.VaultType vaultType) {
        if (vaultType == FileVault.VaultType.OTHERS || vaultType == FileVault.VaultType.CUSTOM) {
            return true; // Accept all files
        }
        
        String mimeType = getContentResolver().getType(fileUri);
        if (mimeType == null) {
            // Try to get MIME type from file extension
            String extension = MimeTypeMap.getFileExtensionFromUrl(fileUri.toString());
            if (extension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
            }
        }
        
        if (mimeType == null) {
            return false; // Cannot determine file type
        }
        
        switch (vaultType) {
            case IMAGES:
                return mimeType.startsWith("image/");
            case PDFS:
                return mimeType.equals("application/pdf");
            case DOCUMENTS:
                return mimeType.equals("application/pdf") ||
                       mimeType.startsWith("text/") ||
                       mimeType.contains("document") ||
                       mimeType.contains("spreadsheet") ||
                       mimeType.contains("presentation") ||
                       mimeType.equals("application/msword") ||
                       mimeType.equals("application/vnd.ms-excel") ||
                       mimeType.equals("application/vnd.ms-powerpoint");
            default:
                return true;
        }
    }
    
    /**
     * Gets a user-friendly description for the vault type
     */
    private String getVaultTypeDescription(FileVault.VaultType vaultType) {
        switch (vaultType) {
            case IMAGES:
                return "image";
            case PDFS:
                return "PDF";
            case DOCUMENTS:
                return "document (PDF, Word, Excel, PowerPoint, text)";
            default:
                return "file";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
