package com.passman.android.ui.notes;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.passman.android.R;
import com.passman.android.data.entity.SecureNoteEntity;
import com.passman.android.databinding.ActivitySecureNoteDetailBinding;

/**
 * Activity for viewing and editing a secure note
 */
public class SecureNoteDetailActivity extends AppCompatActivity {

    private ActivitySecureNoteDetailBinding binding;
    private SecureNotesViewModel viewModel;
    private SecureNoteEntity currentNote;
    private long noteId = -1;

    // Predefined categories
    private static final String[] CATEGORIES = {
            "Personal", "Work", "Finance", "Health", 
            "Travel", "Ideas", "Passwords", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecureNoteDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SecureNotesViewModel.class);

        // Setup category dropdown
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_dropdown_item_1line, CATEGORIES);
        binding.actvCategory.setAdapter(categoryAdapter);

        // Get note ID from intent
        noteId = getIntent().getLongExtra(SecureNotesActivity.EXTRA_NOTE_ID, -1);
        
        if (noteId != -1) {
            getSupportActionBar().setTitle("Edit Note");
            binding.btnDelete.setVisibility(View.VISIBLE);
            loadNote();
        } else {
            getSupportActionBar().setTitle("New Note");
            binding.btnDelete.setVisibility(View.GONE);
            currentNote = new SecureNoteEntity();
        }

        // Favorite toggle
        binding.switchFavorite.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentNote != null) {
                currentNote.setFavorite(isChecked);
            }
        });

        // Pin toggle
        binding.switchPinned.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (currentNote != null) {
                currentNote.setPinned(isChecked);
            }
        });

        // Save button
        binding.btnSave.setOnClickListener(v -> {
            if (saveNote()) {
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        // Delete button
        binding.btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadNote() {
        viewModel.getAllNotes().observe(this, notes -> {
            if (notes != null) {
                for (SecureNoteEntity note : notes) {
                    if (note.getId() == noteId) {
                        currentNote = note;
                        populateFields();
                        break;
                    }
                }
            }
        });
    }

    private void populateFields() {
        if (currentNote == null) return;

        binding.etTitle.setText(currentNote.getTitle());
        binding.etContent.setText(currentNote.getContent());
        binding.etTags.setText(currentNote.getTags());
        binding.switchFavorite.setChecked(currentNote.isFavorite());
        binding.switchPinned.setChecked(currentNote.isPinned());

        // Set category
        if (currentNote.getCategory() != null) {
            binding.actvCategory.setText(currentNote.getCategory(), false);
        }
    }

    private boolean saveNote() {
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();
        String tags = binding.etTags.getText().toString().trim();
        String category = binding.actvCategory.getText().toString().trim();

        if (title.isEmpty()) {
            binding.etTitle.setError("Title is required");
            return false;
        }

        if (currentNote == null) {
            currentNote = new SecureNoteEntity();
        }

        currentNote.setTitle(title);
        currentNote.setContent(content);
        currentNote.setTags(tags);
        currentNote.setCategory(category.isEmpty() ? "Personal" : category);
        currentNote.setUpdatedAt(System.currentTimeMillis());

        if (currentNote.getId() == 0) {
            currentNote.setCreatedAt(System.currentTimeMillis());
        }

        viewModel.saveNote(currentNote);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note_detail, menu);
        
        // Hide delete for new notes
        if (noteId == -1) {
            menu.findItem(R.id.action_delete).setVisible(false);
        }
        
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_save) {
            if (saveNote()) {
                Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
                finish();
            }
            return true;
        } else if (id == R.id.action_delete) {
            confirmDelete();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void confirmDelete() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Delete Note")
                .setMessage("Are you sure you want to delete this note? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (currentNote != null && currentNote.getId() != 0) {
                        viewModel.deleteNote(currentNote);
                        Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
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
