package com.passman.android.ui.qr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.passman.android.R;
import com.passman.android.databinding.ActivityQrScannerBinding;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Activity for scanning QR codes to import credentials
 */
public class QRScannerActivity extends AppCompatActivity {

    private ActivityQrScannerBinding binding;
    
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startQRScanner();
                } else {
                    Toast.makeText(this, "Camera permission is required to scan QR codes", 
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQrScannerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        checkCameraPermission();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Scan QR Code");
        }
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            startQRScanner();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a PassMan QR code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                processQRContent(result.getContents());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void processQRContent(String content) {
        try {
            JSONObject json = new JSONObject(content);
            
            // Validate it's a PassMan QR code
            if (!"PassMan".equals(json.optString("app"))) {
                showError("This QR code is not from PassMan");
                return;
            }

            String type = json.optString("type");
            if (!"credential".equals(type)) {
                showError("Unsupported QR code type");
                return;
            }

            JSONObject data = json.getJSONObject("data");
            String title = data.optString("title", "");
            String username = data.optString("username", "");
            String email = data.optString("email", "");
            String password = data.optString("password", "");
            String url = data.optString("url", "");

            // Show confirmation dialog
            showImportConfirmation(title, username, email, password, url);

        } catch (JSONException e) {
            showError("Invalid QR code format");
        }
    }

    private void showImportConfirmation(String title, String username, String email, 
                                         String password, String url) {
        StringBuilder message = new StringBuilder();
        message.append("Title: ").append(title).append("\n");
        if (!username.isEmpty()) message.append("Username: ").append(username).append("\n");
        if (!email.isEmpty()) message.append("Email: ").append(email).append("\n");
        if (!url.isEmpty()) message.append("URL: ").append(url).append("\n");
        message.append("\nDo you want to import this credential?");

        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle("Import Credential")
                .setMessage(message.toString())
                .setPositiveButton("Import", (dialog, which) -> {
                    // Create intent to add credential
                    Intent intent = new Intent(this, 
                            com.passman.android.ui.credential.AddEditCredentialActivity.class);
                    intent.putExtra("import_title", title);
                    intent.putExtra("import_username", username);
                    intent.putExtra("import_email", email);
                    intent.putExtra("import_password", password);
                    intent.putExtra("import_url", url);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showError(String message) {
        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
