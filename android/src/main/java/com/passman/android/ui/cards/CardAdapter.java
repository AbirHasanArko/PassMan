package com.passman.android.ui.cards;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.passman.android.R;
import com.passman.android.data.entity.CardEntity;
import com.passman.android.databinding.ItemCardBinding;

/**
 * RecyclerView adapter for cards
 */
public class CardAdapter extends ListAdapter<CardEntity, CardAdapter.CardViewHolder> {

    private final OnCardClickListener clickListener;
    private final OnCardFavoriteListener favoriteListener;

    // Card gradient colors by type
    private static final int[][] CARD_COLORS = {
            {0xFF1a237e, 0xFF283593, 0xFF3949ab}, // Credit - Blue
            {0xFF1b5e20, 0xFF2e7d32, 0xFF388e3c}, // Debit - Green
            {0xFF4a148c, 0xFF6a1b9a, 0xFF7b1fa2}, // ID - Purple
            {0xFF006064, 0xFF00838f, 0xFF0097a7}, // Passport - Cyan
            {0xFFbf360c, 0xFFd84315, 0xFFe64a19}, // License - Orange
            {0xFFc62828, 0xFFd32f2f, 0xFFe53935}, // Insurance - Red
            {0xFF4527a0, 0xFF512da8, 0xFF5e35b1}, // Membership - Deep Purple
            {0xFF0d47a1, 0xFF1565c0, 0xFF1976d2}, // Student - Light Blue
            {0xFF37474f, 0xFF455a64, 0xFF546e7a}, // Employee - Grey Blue
            {0xFF424242, 0xFF616161, 0xFF757575}  // Other - Grey
    };

    public interface OnCardClickListener {
        void onCardClick(CardEntity card);
    }

    public interface OnCardFavoriteListener {
        void onFavoriteToggle(CardEntity card);
    }

    public CardAdapter(OnCardClickListener clickListener, OnCardFavoriteListener favoriteListener) {
        super(new DiffUtil.ItemCallback<CardEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull CardEntity oldItem, @NonNull CardEntity newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull CardEntity oldItem, @NonNull CardEntity newItem) {
                return oldItem.getCardName().equals(newItem.getCardName()) &&
                       oldItem.getUpdatedAt() == newItem.getUpdatedAt() &&
                       oldItem.isFavorite() == newItem.isFavorite();
            }
        });
        this.clickListener = clickListener;
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public CardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCardBinding binding = ItemCardBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CardViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener, favoriteListener);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        private final ItemCardBinding binding;

        CardViewHolder(ItemCardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(CardEntity card, OnCardClickListener clickListener, OnCardFavoriteListener favoriteListener) {
            binding.tvCardName.setText(card.getCardName());
            binding.tvCardNumber.setText(card.getMaskedCardNumber());
            binding.tvCardholderName.setText(card.getCardholderName() != null ? 
                    card.getCardholderName().toUpperCase() : "");
            binding.tvExpiry.setText(card.getFormattedExpiry());
            binding.tvIssuer.setText(card.getIssuer() != null ? card.getIssuer() : "");
            
            // Card type emoji
            CardEntity.CardType cardType = getCardType(card.getCardType());
            binding.tvCardEmoji.setText(cardType.getEmoji());
            
            // Card gradient based on type
            int[] colors = getColorsForType(cardType);
            GradientDrawable gradient = new GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    colors
            );
            gradient.setCornerRadius(0);
            binding.cardContainer.setBackground(gradient);
            
            // Favorite icon
            binding.ivFavorite.setImageResource(card.isFavorite() ? 
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
            
            // Expiry warning
            if (card.isExpired()) {
                binding.tvExpiryWarning.setVisibility(View.VISIBLE);
                binding.tvExpiryWarning.setText("❌ Expired");
                binding.tvExpiryWarning.setTextColor(Color.parseColor("#FF6B6B"));
            } else if (card.isExpiringSoon(30)) {
                binding.tvExpiryWarning.setVisibility(View.VISIBLE);
                binding.tvExpiryWarning.setText("⚠️ Expiring Soon");
                binding.tvExpiryWarning.setTextColor(Color.parseColor("#FFD93D"));
            } else {
                binding.tvExpiryWarning.setVisibility(View.GONE);
            }
            
            // Click listeners
            binding.getRoot().setOnClickListener(v -> clickListener.onCardClick(card));
            binding.ivFavorite.setOnClickListener(v -> favoriteListener.onFavoriteToggle(card));
        }

        private CardEntity.CardType getCardType(String typeName) {
            if (typeName == null) return CardEntity.CardType.OTHER;
            try {
                return CardEntity.CardType.valueOf(typeName);
            } catch (Exception e) {
                return CardEntity.CardType.OTHER;
            }
        }

        private int[] getColorsForType(CardEntity.CardType type) {
            int index = type.ordinal();
            if (index >= 0 && index < CARD_COLORS.length) {
                return CARD_COLORS[index];
            }
            return CARD_COLORS[CARD_COLORS.length - 1];
        }
    }
}
