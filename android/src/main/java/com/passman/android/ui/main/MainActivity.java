package com.passman.android.ui.main;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.passman.android.PassManApp;
import com.passman.android.R;
import com.passman.android.data.entity.CredentialEntity;
import com.passman.android.databinding.ActivityMainBinding;
import com.passman.android.ui.auth.AuthActivity;
import com.passman.android.ui.credential.AddEditCredentialActivity;
import com.passman.android.ui.credential.CredentialDetailActivity;
import com.passman.android.ui.generator.PasswordGeneratorActivity;
import com.passman.android.ui.settings.SettingsActivity;
import com.passman.android.ui.notes.SecureNotesActivity;
import com.passman.android.ui.cards.CardsActivity;

/**
 * Main Dashboard Activity
 */
public class MainActivity extends AppCompatActivity implements 
        CredentialAdapter.OnCredentialClickListener,
        FilterSortBottomSheet.FilterSortListener {

    private ActivityMainBinding binding;
    private MainViewModel viewModel;
    private CredentialAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        setupToolbar();
        setupTabs();
        setupRecyclerView();
        setupFab();
        setupSearch();
        setupSwipeRefresh();
        observeViewModel();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.dashboard);
        }
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    viewModel.setFilterType(MainViewModel.FilterType.ALL);
                } else {
                    viewModel.setFilterType(MainViewModel.FilterType.FAVORITES);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new CredentialAdapter(this, viewModel);
        binding.rvCredentials.setLayoutManager(new LinearLayoutManager(this));
        binding.rvCredentials.setAdapter(adapter);
        // Don't use setHasFixedSize(true) - RecyclerView is inside NestedScrollView with wrap_content
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditCredentialActivity.class);
            startActivity(intent);
        });

        // Hide/show FAB on scroll
        binding.rvCredentials.addOnScrollListener(new androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(androidx.recyclerview.widget.RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && binding.fabAdd.isShown()) {
                    binding.fabAdd.hide();
                } else if (dy < 0 && !binding.fabAdd.isShown()) {
                    binding.fabAdd.show();
                }
            }
        });
    }

    private void setupSearch() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                binding.searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(
                R.color.secondary,
                R.color.primary
        );
        binding.swipeRefresh.setOnRefreshListener(() -> {
            // Data is automatically refreshed via LiveData
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void observeViewModel() {
        // Credentials list
        viewModel.getFilteredCredentials().observe(this, credentials -> {
            // Submit a new copy to ensure DiffUtil properly computes differences
            adapter.submitList(credentials == null ? null : new java.util.ArrayList<>(credentials), () -> {
                // Callback after diff is computed and list is updated
                binding.rvCredentials.post(() -> binding.rvCredentials.requestLayout());
            });
            
            // Show/hide empty state
            if (credentials == null || credentials.isEmpty()) {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.rvCredentials.setVisibility(View.GONE);
            } else {
                binding.emptyState.setVisibility(View.GONE);
                binding.rvCredentials.setVisibility(View.VISIBLE);
            }
        });

        // Statistics for header card
        viewModel.getTotalCount().observe(this, count -> {
            binding.tvTotalCount.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getWeakPasswordCount().observe(this, count -> {
            binding.tvWeakCount.setText(String.valueOf(count != null ? count : 0));
        });

        viewModel.getAverageStrength().observe(this, score -> {
            int safeScore = score != null ? score : 0;
            binding.progressSecurityScore.setProgress(safeScore);
            binding.tvSecurityScore.setText(safeScore + "%");
            updateSecurityScoreColor(safeScore);
        });

        // Error messages
        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    private void updateSecurityScoreColor(int score) {
        int color;
        if (score < 30) {
            color = getColor(R.color.strength_weak);
        } else if (score < 50) {
            color = getColor(R.color.strength_fair);
        } else if (score < 70) {
            color = getColor(R.color.strength_good);
        } else if (score < 85) {
            color = getColor(R.color.strength_strong);
        } else {
            color = getColor(R.color.strength_very_strong);
        }
        binding.progressSecurityScore.setIndicatorColor(color);
    }

    // ==================== ADAPTER CALLBACKS ====================

    @Override
    public void onCredentialClick(CredentialEntity credential) {
        Intent intent = new Intent(this, CredentialDetailActivity.class);
        intent.putExtra("credential_id", credential.getId());
        startActivity(intent);
    }

    @Override
    public void onCopyPassword(CredentialEntity credential) {
        String password = viewModel.decryptPassword(credential);
        if (password != null) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("password", password);
            clipboard.setPrimaryClip(clip);
            
            Snackbar.make(binding.getRoot(), R.string.copied_to_clipboard, Snackbar.LENGTH_SHORT)
                    .setAction("Clear", v -> clipboard.setPrimaryClip(ClipData.newPlainText("", "")))
                    .show();

            // Auto-clear clipboard after 30 seconds
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                clipboard.setPrimaryClip(ClipData.newPlainText("", ""));
            }, 30000);
        }
    }

    @Override
    public void onToggleFavorite(CredentialEntity credential) {
        viewModel.toggleFavorite(credential);
    }

    @Override
    public void onDeleteCredential(CredentialEntity credential) {
        new MaterialAlertDialogBuilder(this, R.style.Theme_PassMan_Dialog)
                .setTitle(R.string.confirm_delete)
                .setMessage(R.string.confirm_delete_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteCredential(credential);
                    Snackbar.make(binding.getRoot(), R.string.credential_deleted, Snackbar.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ==================== MENU ====================

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_filter) {
            showFilterSortBottomSheet();
            return true;
        } else if (id == R.id.action_generator) {
            startActivity(new Intent(this, PasswordGeneratorActivity.class));
            return true;
        } else if (id == R.id.action_scan_qr) {
            startActivity(new Intent(this, com.passman.android.ui.qr.QRScannerActivity.class));
            return true;
        } else if (id == R.id.action_vault) {
            startActivity(new Intent(this, com.passman.android.ui.vault.FileVaultActivity.class));
            return true;
        } else if (id == R.id.action_notes) {
            startActivity(new Intent(this, SecureNotesActivity.class));
            return true;
        } else if (id == R.id.action_cards) {
            startActivity(new Intent(this, CardsActivity.class));
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_lock) {
            lockVault();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void lockVault() {
        viewModel.lockVault();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    
    private void showFilterSortBottomSheet() {
        FilterSortBottomSheet.FilterSortOptions currentOptions = viewModel.getCurrentFilterSortOptions();
        FilterSortBottomSheet bottomSheet = FilterSortBottomSheet.newInstance(currentOptions);
        bottomSheet.setListener(this);
        bottomSheet.show(getSupportFragmentManager(), "FilterSortBottomSheet");
    }
    
    @Override
    public void onFilterSortApplied(FilterSortBottomSheet.FilterSortOptions options) {
        viewModel.setFilterSortOptions(options);
        
        // Show feedback
        if (options.hasActiveFilters()) {
            Snackbar.make(binding.getRoot(), R.string.filters_applied, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(binding.getRoot(), R.string.filters_cleared, Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Update session activity
        ((PassManApp) getApplication()).getSessionManager().updateLastActive();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
