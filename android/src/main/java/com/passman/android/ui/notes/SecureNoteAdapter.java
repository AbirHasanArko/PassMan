package com.passman.android.ui.notes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.passman.android.R;
import com.passman.android.data.entity.SecureNoteEntity;
import com.passman.android.databinding.ItemSecureNoteBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecyclerView adapter for secure notes
 */
public class SecureNoteAdapter extends ListAdapter<SecureNoteEntity, SecureNoteAdapter.NoteViewHolder> {

    private final OnNoteClickListener clickListener;
    private final OnNoteFavoriteListener favoriteListener;

    public interface OnNoteClickListener {
        void onNoteClick(SecureNoteEntity note);
    }

    public interface OnNoteFavoriteListener {
        void onFavoriteToggle(SecureNoteEntity note);
    }

    public SecureNoteAdapter(OnNoteClickListener clickListener, OnNoteFavoriteListener favoriteListener) {
        super(new DiffUtil.ItemCallback<SecureNoteEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull SecureNoteEntity oldItem, @NonNull SecureNoteEntity newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @Override
            public boolean areContentsTheSame(@NonNull SecureNoteEntity oldItem, @NonNull SecureNoteEntity newItem) {
                return oldItem.getTitle().equals(newItem.getTitle()) &&
                       oldItem.getUpdatedAt() == newItem.getUpdatedAt() &&
                       oldItem.isFavorite() == newItem.isFavorite() &&
                       oldItem.isPinned() == newItem.isPinned();
            }
        });
        this.clickListener = clickListener;
        this.favoriteListener = favoriteListener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSecureNoteBinding binding = ItemSecureNoteBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new NoteViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.bind(getItem(position), clickListener, favoriteListener);
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        private final ItemSecureNoteBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

        NoteViewHolder(ItemSecureNoteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(SecureNoteEntity note, OnNoteClickListener clickListener, OnNoteFavoriteListener favoriteListener) {
            binding.tvTitle.setText(note.getTitle());
            binding.tvContent.setText(note.getContent());
            binding.tvDate.setText(dateFormat.format(new Date(note.getUpdatedAt())));
            
            // Pin icon
            binding.ivPinned.setVisibility(note.isPinned() ? View.VISIBLE : View.GONE);
            
            // Favorite icon
            binding.ivFavorite.setImageResource(note.isFavorite() ? 
                    R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
            
            // Category
            if (note.getCategory() != null && !note.getCategory().isEmpty()) {
                binding.tvCategory.setVisibility(View.VISIBLE);
                binding.tvCategory.setText(note.getCategory());
            } else {
                binding.tvCategory.setVisibility(View.GONE);
            }
            
            // Color (if set)
            if (note.getColor() != null && !note.getColor().isEmpty()) {
                try {
                    int color = android.graphics.Color.parseColor(note.getColor());
                    binding.noteContainer.setBackgroundColor(color);
                } catch (Exception ignored) {}
            }
            
            // Click listeners
            binding.getRoot().setOnClickListener(v -> clickListener.onNoteClick(note));
            binding.ivFavorite.setOnClickListener(v -> favoriteListener.onFavoriteToggle(note));
        }
    }
}
