package com.passman.android.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.passman.android.R;
import com.passman.android.data.entity.CredentialEntity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * RecyclerView Adapter for credentials list
 */
public class CredentialAdapter extends ListAdapter<CredentialEntity, CredentialAdapter.CredentialViewHolder> {

    private final OnCredentialClickListener listener;
    private final MainViewModel viewModel;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public interface OnCredentialClickListener {
        void onCredentialClick(CredentialEntity credential);
        void onCopyPassword(CredentialEntity credential);
        void onToggleFavorite(CredentialEntity credential);
        void onDeleteCredential(CredentialEntity credential);
    }

    public CredentialAdapter(OnCredentialClickListener listener, MainViewModel viewModel) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        this.viewModel = viewModel;
    }

    private static final DiffUtil.ItemCallback<CredentialEntity> DIFF_CALLBACK = 
            new DiffUtil.ItemCallback<CredentialEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull CredentialEntity oldItem, 
                                        @NonNull CredentialEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull CredentialEntity oldItem, 
                                          @NonNull CredentialEntity newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                   oldItem.isFavorite() == newItem.isFavorite() &&
                   oldItem.getLastModified() == newItem.getLastModified();
        }
    };

    @NonNull
    @Override
    public CredentialViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_credential, parent, false);
        return new CredentialViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CredentialViewHolder holder, int position) {
        CredentialEntity credential = getItem(position);
        holder.bind(credential);
    }

    class CredentialViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final ImageView ivIcon;
        private final TextView tvTitle;
        private final TextView tvSubtitle;
        private final TextView tvLastModified;
        private final ImageButton btnFavorite;
        private final ImageButton btnCopy;
        private final View strengthIndicator;

        CredentialViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardCredential);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvSubtitle = itemView.findViewById(R.id.tvSubtitle);
            tvLastModified = itemView.findViewById(R.id.tvLastModified);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
            btnCopy = itemView.findViewById(R.id.btnCopy);
            strengthIndicator = itemView.findViewById(R.id.strengthIndicator);
        }

        void bind(CredentialEntity credential) {
            // Title
            tvTitle.setText(credential.getTitle());

            // Subtitle (username or email)
            String subtitle = credential.getUsername();
            if (subtitle == null || subtitle.isEmpty()) {
                subtitle = credential.getEmail();
            }
            if (subtitle == null || subtitle.isEmpty()) {
                subtitle = credential.getUrl();
            }
            tvSubtitle.setText(subtitle != null ? subtitle : "");

            // Last modified
            if (credential.getLastModified() > 0) {
                String dateStr = dateFormat.format(new Date(credential.getLastModified()));
                tvLastModified.setText(dateStr);
            }

            // Favorite icon
            updateFavoriteIcon(credential.isFavorite());

            // Icon based on URL or category
            setCredentialIcon(credential);

            // Strength indicator color
            setStrengthIndicator(credential.getPasswordStrengthScore());

            // Click listeners
            cardView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCredentialClick(credential);
                }
            });

            cardView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteCredential(credential);
                }
                return true;
            });

            btnFavorite.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onToggleFavorite(credential);
                    updateFavoriteIcon(!credential.isFavorite());
                }
            });

            btnCopy.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCopyPassword(credential);
                }
            });
        }

        private void updateFavoriteIcon(boolean isFavorite) {
            if (isFavorite) {
                btnFavorite.setImageResource(R.drawable.ic_favorite_filled);
                btnFavorite.setColorFilter(itemView.getContext().getColor(R.color.warning));
            } else {
                btnFavorite.setImageResource(R.drawable.ic_favorite_outline);
                btnFavorite.setColorFilter(itemView.getContext().getColor(R.color.icon_secondary_light));
            }
        }

        private void setCredentialIcon(CredentialEntity credential) {
            String url = credential.getUrl();
            String category = credential.getCategory();
            
            int iconRes = R.drawable.ic_key; // default
            int bgColor = R.color.primary;

            if (url != null) {
                String lowerUrl = url.toLowerCase();
                if (lowerUrl.contains("google")) {
                    iconRes = R.drawable.ic_google;
                    bgColor = R.color.badge_social;
                } else if (lowerUrl.contains("facebook") || lowerUrl.contains("meta")) {
                    iconRes = R.drawable.ic_facebook;
                    bgColor = R.color.badge_social;
                } else if (lowerUrl.contains("twitter") || lowerUrl.contains("x.com")) {
                    iconRes = R.drawable.ic_twitter;
                    bgColor = R.color.badge_social;
                } else if (lowerUrl.contains("github")) {
                    iconRes = R.drawable.ic_github;
                    bgColor = R.color.badge_work;
                } else if (lowerUrl.contains("amazon") || lowerUrl.contains("shopping")) {
                    iconRes = R.drawable.ic_shopping;
                    bgColor = R.color.badge_shopping;
                } else if (lowerUrl.contains("bank") || lowerUrl.contains("paypal")) {
                    iconRes = R.drawable.ic_bank;
                    bgColor = R.color.badge_finance;
                }
            }

            ivIcon.setImageResource(iconRes);
            ivIcon.setBackgroundTintList(
                    itemView.getContext().getColorStateList(bgColor));
        }

        private void setStrengthIndicator(int score) {
            int color;
            if (score < 30) {
                color = R.color.strength_weak;
            } else if (score < 50) {
                color = R.color.strength_fair;
            } else if (score < 70) {
                color = R.color.strength_good;
            } else {
                color = R.color.strength_strong;
            }
            strengthIndicator.setBackgroundColor(
                    itemView.getContext().getColor(color));
        }
    }
}
