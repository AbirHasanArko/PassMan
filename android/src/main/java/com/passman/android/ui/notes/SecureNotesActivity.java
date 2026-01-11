package com.passman.android.ui.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.passman.android.R;
import com.passman.android.data.entity.SecureNoteEntity;
import com.passman.android.databinding.ActivitySecureNotesBinding;

import java.util.List;

/**
 * Activity for listing and managing secure notes
 */
public class SecureNotesActivity extends AppCompatActivity {

    public static final String EXTRA_NOTE_ID = "note_id";

    private ActivitySecureNotesBinding binding;
    private SecureNotesViewModel viewModel;
    private SecureNoteAdapter adapter;

    // Predefined categories
    private static final String[] CATEGORIES = {
            "All", "Personal", "Work", "Finance", "Health", 
            "Travel", "Ideas", "Passwords", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySecureNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Secure Notes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(SecureNotesViewModel.class);

        // Setup RecyclerView with staggered grid
        adapter = new SecureNoteAdapter(
                note -> openNoteDetail(note),
                note -> viewModel.toggleFavorite(note)
        );
        binding.rvNotes.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        binding.rvNotes.setAdapter(adapter);

        // Setup category chips
        setupCategoryChips();

        // Setup search
        binding.etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(android.text.Editable s) {
                viewModel.setSearchQuery(s.toString());
            }
        });

        // Observe data
        viewModel.getFilteredNotes().observe(this, notes -> {
            if (notes == null || notes.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvNotes.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvNotes.setVisibility(View.VISIBLE);
                adapter.submitList(sortNotes(notes));
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

        // FAB click
        binding.fabAddNote.setOnClickListener(v -> {
            Intent intent = new Intent(this, SecureNoteDetailActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshNotes();
    }

    private void setupCategoryChips() {
        binding.chipGroupCategories.removeAllViews();
        for (String category : CATEGORIES) {
            Chip chip = new Chip(this);
            chip.setText(category);
            chip.setCheckable(true);
            chip.setCheckedIconVisible(true);
            
            if (category.equals("All")) {
                chip.setChecked(true);
            }
            
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    // Uncheck other chips
                    for (int i = 0; i < binding.chipGroupCategories.getChildCount(); i++) {
                        Chip otherChip = (Chip) binding.chipGroupCategories.getChildAt(i);
                        if (otherChip != chip) {
                            otherChip.setChecked(false);
                        }
                    }
                    viewModel.setFilterCategory(category.equals("All") ? null : category);
                }
            });
            
            binding.chipGroupCategories.addView(chip);
        }
    }

    private List<SecureNoteEntity> sortNotes(List<SecureNoteEntity> notes) {
        // Sort by pinned first, then by updated date
        notes.sort((a, b) -> {
            if (a.isPinned() != b.isPinned()) {
                return a.isPinned() ? -1 : 1;
            }
            return Long.compare(b.getUpdatedAt(), a.getUpdatedAt());
        });
        return notes;
    }

    private void openNoteDetail(SecureNoteEntity note) {
        Intent intent = new Intent(this, SecureNoteDetailActivity.class);
        intent.putExtra(EXTRA_NOTE_ID, note.getId());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
