package com.passman.android.ui.cards;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;
import com.passman.android.data.entity.CardEntity;
import com.passman.android.databinding.ActivityCardsBinding;

import java.util.List;

/**
 * Activity for listing and managing cards (credit, debit, ID, etc.)
 */
public class CardsActivity extends AppCompatActivity {

    public static final String EXTRA_CARD_ID = "card_id";

    private ActivityCardsBinding binding;
    private CardsViewModel viewModel;
    private CardAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCardsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setup toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("My Cards");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(CardsViewModel.class);

        // Setup RecyclerView
        adapter = new CardAdapter(
                card -> openCardDetail(card),
                card -> viewModel.toggleFavorite(card)
        );
        binding.rvCards.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCards.setAdapter(adapter);

        // Setup type filter chips
        setupTypeChips();

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
        viewModel.getFilteredCards().observe(this, cards -> {
            if (cards == null || cards.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvCards.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvCards.setVisibility(View.VISIBLE);
                adapter.submitList(sortCards(cards));
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
        binding.fabAddCard.setOnClickListener(v -> {
            Intent intent = new Intent(this, CardDetailActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.refreshCards();
    }

    private void setupTypeChips() {
        binding.chipGroupTypes.removeAllViews();
        
        // All chip
        Chip allChip = new Chip(this);
        allChip.setText("All");
        allChip.setCheckable(true);
        allChip.setChecked(true);
        allChip.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                uncheckOtherChips(allChip);
                viewModel.setFilterType(null);
            }
        });
        binding.chipGroupTypes.addView(allChip);

        // Add chip for each card type
        for (CardEntity.CardType type : CardEntity.CardType.values()) {
            Chip chip = new Chip(this);
            chip.setText(type.getEmoji() + " " + type.getDisplayName());
            chip.setCheckable(true);
            chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    uncheckOtherChips(chip);
                    viewModel.setFilterType(type.name());
                }
            });
            binding.chipGroupTypes.addView(chip);
        }
    }

    private void uncheckOtherChips(Chip selectedChip) {
        for (int i = 0; i < binding.chipGroupTypes.getChildCount(); i++) {
            Chip chip = (Chip) binding.chipGroupTypes.getChildAt(i);
            if (chip != selectedChip) {
                chip.setChecked(false);
            }
        }
    }

    private List<CardEntity> sortCards(List<CardEntity> cards) {
        // Sort favorites first, then by name
        cards.sort((a, b) -> {
            if (a.isFavorite() != b.isFavorite()) {
                return a.isFavorite() ? -1 : 1;
            }
            return a.getCardName().compareToIgnoreCase(b.getCardName());
        });
        return cards;
    }

    private void openCardDetail(CardEntity card) {
        Intent intent = new Intent(this, CardDetailActivity.class);
        intent.putExtra(EXTRA_CARD_ID, card.getId());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
