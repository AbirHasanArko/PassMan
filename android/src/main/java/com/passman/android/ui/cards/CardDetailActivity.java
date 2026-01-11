package com.passman.android.ui.cards;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.passman.android.R;
import com.passman.android.data.entity.CardEntity;
import com.passman.android.databinding.ActivityCardDetailBinding;

/**
 * Activity for viewing and editing a card (credit, debit, ID, etc.)
 */
public class CardDetailActivity extends AppCompatActivity {

    private ActivityCardDetailBinding binding;
    private CardsViewModel viewModel;
    private CardEntity currentCard;
    private long cardId = -1;

    private boolean isScanningFront = true;

    // Camera launcher
    private final ActivityResultLauncher<Intent> cameraLauncher = 
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Bundle extras = result.getData().getExtras();
                    if (extras != null) {
                        Bitmap imageBitmap = (Bitmap) extras.get("data");
                        if (imageBitmap != null) {
                            processScannedImage(imageBitmap);
                        }
                    }
                }
            });

    // Permission launcher
    private final ActivityResultLauncher<String> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchCamera();
                } else {
                    Toast.makeText(this, "Camera permission required for scanning", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCardDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CardsViewModel.class);

        // Setup card type dropdown
        String[] typeNames = new String[CardEntity.CardType.values().length];
        for (int i = 0; i < CardEntity.CardType.values().length; i++) {
            CardEntity.CardType type = CardEntity.CardType.values()[i];
            typeNames[i] = type.getEmoji() + " " + type.getDisplayName();
        }
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, typeNames);
        binding.actvCardType.setAdapter(typeAdapter);

        // Get card ID from intent
        cardId = getIntent().getLongExtra(CardsActivity.EXTRA_CARD_ID, -1);

        if (cardId != -1) {
            getSupportActionBar().setTitle("Edit Card");
            binding.btnDelete.setVisibility(View.VISIBLE);
            loadCard();
        } else {
            getSupportActionBar().setTitle("New Card");
            binding.btnDelete.setVisibility(View.GONE);
            currentCard = new CardEntity();
            // Set default values
            binding.etReminderDays.setText("30");
        }

        // Setup listeners
        setupListeners();
    }

    private void setupListeners() {
        // Scan front of card
        binding.btnScanFront.setOnClickListener(v -> {
            isScanningFront = true;
            requestCameraPermission();
        });

        // Scan back of card
        binding.btnScanBack.setOnClickListener(v -> {
            isScanningFront = false;
            requestCameraPermission();
        });

        // View front image
        binding.btnViewFront.setOnClickListener(v -> {
            if (currentCard != null && currentCard.getFrontImagePath() != null) {
                showCardImage(currentCard.getFrontImagePath(), "Card Front");
            }
        });

        // View back image
        binding.btnViewBack.setOnClickListener(v -> {
            if (currentCard != null && currentCard.getBackImagePath() != null) {
                showCardImage(currentCard.getBackImagePath(), "Card Back");
            }
        });

        // Save button
        binding.btnSave.setOnClickListener(v -> saveCard());

        // Delete button
        binding.btnDelete.setOnClickListener(v -> confirmDelete());

        // Favorite toggle
        binding.switchFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentCard != null) {
                currentCard.setFavorite(isChecked);
            }
        });
    }

    private void loadCard() {
        viewModel.getAllCards().observe(this, cards -> {
            if (cards != null) {
                for (CardEntity card : cards) {
                    if (card.getId() == cardId) {
                        currentCard = card;
                        populateFields();
                        break;
                    }
                }
            }
        });
    }

    private void populateFields() {
        if (currentCard == null) return;

        binding.etCardName.setText(currentCard.getCardName());
        binding.etCardNumber.setText(currentCard.getCardNumber());
        binding.etCardholderName.setText(currentCard.getCardholderName());
        binding.etCvv.setText(currentCard.getCvv());
        binding.etPin.setText(currentCard.getPin());
        binding.etIssuer.setText(currentCard.getIssuer());
        binding.etNotes.setText(currentCard.getNotes());
        binding.switchFavorite.setChecked(currentCard.isFavorite());

        // Set card type
        String cardType = currentCard.getCardType();
        if (cardType != null) {
            for (int i = 0; i < CardEntity.CardType.values().length; i++) {
                CardEntity.CardType type = CardEntity.CardType.values()[i];
                if (type.name().equals(cardType)) {
                    binding.actvCardType.setText(type.getEmoji() + " " + type.getDisplayName(), false);
                    break;
                }
            }
        }

        // Set expiry
        if (currentCard.getExpiryMonth() > 0) {
            binding.etExpiryMonth.setText(String.format("%02d", currentCard.getExpiryMonth()));
        }
        if (currentCard.getExpiryYear() > 0) {
            binding.etExpiryYear.setText(String.valueOf(currentCard.getExpiryYear()));
        }

        // Set reminder days
        binding.etReminderDays.setText(String.valueOf(currentCard.getRenewalReminderDays()));

        // Show scan status and view buttons
        if (currentCard.getFrontImagePath() != null && !currentCard.getFrontImagePath().isEmpty()) {
            binding.tvFrontStatus.setText("✅ Scanned");
            binding.btnViewFront.setVisibility(View.VISIBLE);
        } else {
            binding.btnViewFront.setVisibility(View.GONE);
        }
        if (currentCard.getBackImagePath() != null && !currentCard.getBackImagePath().isEmpty()) {
            binding.tvBackStatus.setText("✅ Scanned");
            binding.btnViewBack.setVisibility(View.VISIBLE);
        } else {
            binding.btnViewBack.setVisibility(View.GONE);
        }
    }

    private void showCardImage(String encryptedPath, String title) {
        // Inflate dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_view_card_image, null);
        android.widget.ImageView imageView = dialogView.findViewById(R.id.ivCardImage);
        android.widget.ProgressBar progressBar = dialogView.findViewById(R.id.progressBar);
        android.widget.TextView tvTitle = dialogView.findViewById(R.id.tvTitle);
        android.widget.TextView tvError = dialogView.findViewById(R.id.tvError);
        
        tvTitle.setText(title);

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();
        dialog.show();

        // Decrypt and load image in background
        new Thread(() -> {
            try {
                com.passman.android.security.FileEncryptionManager encryptionManager = 
                        new com.passman.android.security.FileEncryptionManager(getApplication());
                byte[] decryptedData = encryptionManager.decryptFileFromPath(encryptedPath);
                
                // The decrypted data is a PDF, we need to render it as an image
                android.graphics.pdf.PdfRenderer renderer = null;
                android.graphics.Bitmap bitmap = null;
                
                // Write to temp file for PDF rendering
                java.io.File tempFile = new java.io.File(getCacheDir(), "temp_card.pdf");
                try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                    fos.write(decryptedData);
                }
                
                // Render PDF to bitmap
                android.os.ParcelFileDescriptor pfd = android.os.ParcelFileDescriptor.open(
                        tempFile, android.os.ParcelFileDescriptor.MODE_READ_ONLY);
                renderer = new android.graphics.pdf.PdfRenderer(pfd);
                android.graphics.pdf.PdfRenderer.Page page = renderer.openPage(0);
                
                bitmap = android.graphics.Bitmap.createBitmap(
                        page.getWidth() * 2, page.getHeight() * 2, 
                        android.graphics.Bitmap.Config.ARGB_8888);
                bitmap.eraseColor(android.graphics.Color.WHITE);
                page.render(bitmap, null, null, android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                page.close();
                renderer.close();
                pfd.close();
                
                // Delete temp file
                tempFile.delete();
                
                android.graphics.Bitmap finalBitmap = bitmap;
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    imageView.setImageBitmap(finalBitmap);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("Failed to load: " + e.getMessage());
                });
            }
        }).start();
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            cameraLauncher.launch(takePictureIntent);
        } else {
            Toast.makeText(this, "No camera app available", Toast.LENGTH_SHORT).show();
        }
    }

    private void processScannedImage(Bitmap bitmap) {
        if (currentCard == null) {
            currentCard = new CardEntity();
        }

        // First save the card if it doesn't have an ID
        if (currentCard.getId() == 0) {
            if (!validateAndPopulateCard()) {
                Toast.makeText(this, "Please fill in card name first", Toast.LENGTH_SHORT).show();
                return;
            }
            
            viewModel.saveCard(currentCard, new CardsViewModel.SaveCallback() {
                @Override
                public void onSuccess(long savedCardId) {
                    currentCard.setId(savedCardId);
                    cardId = savedCardId;
                    saveScannedImage(bitmap);
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> Toast.makeText(CardDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
                }
            });
        } else {
            saveScannedImage(bitmap);
        }
    }

    private void saveScannedImage(Bitmap bitmap) {
        viewModel.saveScannedImage(bitmap, currentCard.getId(), isScanningFront, 
                new CardsViewModel.ScanCallback() {
            @Override
            public void onSuccess(String filePath) {
                runOnUiThread(() -> {
                    if (isScanningFront) {
                        currentCard.setFrontImagePath(filePath);
                        binding.tvFrontStatus.setText("✅ Scanned");
                        binding.btnViewFront.setVisibility(View.VISIBLE);
                    } else {
                        currentCard.setBackImagePath(filePath);
                        binding.tvBackStatus.setText("✅ Scanned");
                        binding.btnViewBack.setVisibility(View.VISIBLE);
                    }
                    Toast.makeText(CardDetailActivity.this, 
                            "Card " + (isScanningFront ? "front" : "back") + " saved", 
                            Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(CardDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private boolean validateAndPopulateCard() {
        String cardName = binding.etCardName.getText().toString().trim();
        if (cardName.isEmpty()) {
            binding.etCardName.setError("Card name is required");
            return false;
        }

        currentCard.setCardName(cardName);
        currentCard.setCardNumber(binding.etCardNumber.getText().toString().trim());
        currentCard.setCardholderName(binding.etCardholderName.getText().toString().trim());
        currentCard.setCvv(binding.etCvv.getText().toString().trim());
        currentCard.setPin(binding.etPin.getText().toString().trim());
        currentCard.setIssuer(binding.etIssuer.getText().toString().trim());
        currentCard.setNotes(binding.etNotes.getText().toString().trim());

        // Card type
        String selectedTypeText = binding.actvCardType.getText().toString();
        CardEntity.CardType selectedType = CardEntity.CardType.CREDIT; // default
        for (CardEntity.CardType type : CardEntity.CardType.values()) {
            if (selectedTypeText.contains(type.getDisplayName())) {
                selectedType = type;
                break;
            }
        }
        currentCard.setCardType(selectedType.name());

        // Expiry
        try {
            String monthStr = binding.etExpiryMonth.getText().toString().trim();
            String yearStr = binding.etExpiryYear.getText().toString().trim();
            if (!monthStr.isEmpty()) {
                currentCard.setExpiryMonth(Integer.parseInt(monthStr));
            }
            if (!yearStr.isEmpty()) {
                currentCard.setExpiryYear(Integer.parseInt(yearStr));
            }
        } catch (NumberFormatException e) {
            // Ignore parsing errors
        }

        // Reminder days
        try {
            String reminderStr = binding.etReminderDays.getText().toString().trim();
            if (!reminderStr.isEmpty()) {
                currentCard.setRenewalReminderDays(Integer.parseInt(reminderStr));
            }
        } catch (NumberFormatException e) {
            currentCard.setRenewalReminderDays(30);
        }

        currentCard.setUpdatedAt(System.currentTimeMillis());
        if (currentCard.getId() == 0) {
            currentCard.setCreatedAt(System.currentTimeMillis());
        }

        return true;
    }

    private void saveCard() {
        if (!validateAndPopulateCard()) {
            return;
        }

        viewModel.saveCard(currentCard, new CardsViewModel.SaveCallback() {
            @Override
            public void onSuccess(long cardId) {
                runOnUiThread(() -> {
                    Toast.makeText(CardDetailActivity.this, "Card saved", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(CardDetailActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_card_detail, menu);

        // Hide delete for new cards
        if (cardId == -1) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            saveCard();
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Card")
                .setMessage("Are you sure you want to delete this card? This will also remove any scanned images.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (currentCard != null && currentCard.getId() != 0) {
                        viewModel.deleteCard(currentCard);
                        Toast.makeText(this, "Card deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
