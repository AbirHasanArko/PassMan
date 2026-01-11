package com.passman.android.ui.vault;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.passman.android.databinding.ItemFileVaultBinding;
import com.passman.core.model.FileVault;

/**
 * RecyclerView adapter for displaying file vaults
 */
public class FileVaultAdapter extends ListAdapter<FileVault, FileVaultAdapter.VaultViewHolder> {

    private final OnVaultClickListener listener;

    public interface OnVaultClickListener {
        void onVaultClick(FileVault vault);
    }

    public FileVaultAdapter(OnVaultClickListener listener) {
        super(new DiffUtil.ItemCallback<FileVault>() {
            @Override
            public boolean areItemsTheSame(@NonNull FileVault oldItem, @NonNull FileVault newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull FileVault oldItem, @NonNull FileVault newItem) {
                return oldItem.getVaultName().equals(newItem.getVaultName());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public VaultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFileVaultBinding binding = ItemFileVaultBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new VaultViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull VaultViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    static class VaultViewHolder extends RecyclerView.ViewHolder {
        private final ItemFileVaultBinding binding;

        VaultViewHolder(ItemFileVaultBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FileVault vault, OnVaultClickListener listener) {
            binding.tvVaultName.setText(vault.getVaultName());
            binding.tvVaultType.setText(formatVaultType(vault.getVaultType()));
            binding.tvVaultEmoji.setText(vault.getIconEmoji() != null ? vault.getIconEmoji() : "📁");
            binding.getRoot().setOnClickListener(v -> listener.onVaultClick(vault));
        }

        private String formatVaultType(com.passman.core.model.FileVault.VaultType type) {
            if (type == null) return "Files";
            switch (type) {
                case IMAGES: return "Images";
                case PDFS: return "PDFs";
                case DOCUMENTS: return "Documents";
                case OTHERS: return "Other Files";
                case CUSTOM: return "Custom";
                default: return type.name();
            }
        }
    }
}
