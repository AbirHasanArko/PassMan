package com.passman.android.ui.vault;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.passman.android.databinding.ItemEncryptedFileBinding;
import com.passman.core.model.EncryptedFile;

/**
 * RecyclerView adapter for displaying encrypted files
 */
public class EncryptedFileAdapter extends ListAdapter<EncryptedFile, EncryptedFileAdapter.FileViewHolder> {

    private final OnFileClickListener listener;
    private final OnFileDeleteListener deleteListener;

    public interface OnFileClickListener {
        void onFileClick(EncryptedFile file);
    }

    public interface OnFileDeleteListener {
        void onFileDelete(EncryptedFile file);
    }

    public EncryptedFileAdapter(OnFileClickListener listener, OnFileDeleteListener deleteListener) {
        super(new DiffUtil.ItemCallback<EncryptedFile>() {
            @Override
            public boolean areItemsTheSame(@NonNull EncryptedFile oldItem, @NonNull EncryptedFile newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull EncryptedFile oldItem, @NonNull EncryptedFile newItem) {
                return oldItem.getOriginalFileName().equals(newItem.getOriginalFileName());
            }
        });
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemEncryptedFileBinding binding = ItemEncryptedFileBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new FileViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.bind(getItem(position), listener, deleteListener);
    }

    static class FileViewHolder extends RecyclerView.ViewHolder {
        private final ItemEncryptedFileBinding binding;

        FileViewHolder(ItemEncryptedFileBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(EncryptedFile file, OnFileClickListener listener, OnFileDeleteListener deleteListener) {
            binding.tvFileName.setText(file.getOriginalFileName());
            binding.tvFileSize.setText(formatFileSize(file.getOriginalSize()));
            binding.tvUploadDate.setText(file.getUploadedAt() != null ? file.getUploadedAt().toString() : "");
            binding.btnDownload.setOnClickListener(v -> listener.onFileClick(file));
            binding.btnDelete.setOnClickListener(v -> deleteListener.onFileDelete(file));
        }

        private String formatFileSize(long bytes) {
            if (bytes <= 0) return "0 B";
            final String[] units = new String[]{"B", "KB", "MB", "GB"};
            int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
            return String.format("%.1f %s", bytes / Math.pow(1024, digitGroups), units[digitGroups]);
        }
    }
}
