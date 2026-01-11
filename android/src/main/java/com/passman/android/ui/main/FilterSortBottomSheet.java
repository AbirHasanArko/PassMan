package com.passman.android.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.passman.android.R;
import com.passman.android.databinding.BottomSheetFilterSortBinding;

/**
 * Bottom sheet dialog for advanced filtering and sorting options
 */
public class FilterSortBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetFilterSortBinding binding;
    private FilterSortListener listener;
    
    // Current selections
    private SortOption currentSort = SortOption.RECENT;
    private String currentCategory = null; // null = all
    private StrengthFilter currentStrength = StrengthFilter.ALL;
    private boolean filterOldPasswords = false;
    private boolean filterReused = false;
    private boolean filterBreached = false;

    public interface FilterSortListener {
        void onFilterSortApplied(FilterSortOptions options);
    }

    public static FilterSortBottomSheet newInstance(FilterSortOptions currentOptions) {
        FilterSortBottomSheet fragment = new FilterSortBottomSheet();
        Bundle args = new Bundle();
        if (currentOptions != null) {
            args.putSerializable("options", currentOptions);
        }
        fragment.setArguments(args);
        return fragment;
    }

    public void setListener(FilterSortListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.Theme_PassMan_BottomSheet);
        
        if (getArguments() != null && getArguments().containsKey("options")) {
            FilterSortOptions options = (FilterSortOptions) getArguments().getSerializable("options");
            if (options != null) {
                currentSort = options.getSortOption();
                currentCategory = options.getCategory();
                currentStrength = options.getStrengthFilter();
                filterOldPasswords = options.isFilterOldPasswords();
                filterReused = options.isFilterReused();
                filterBreached = options.isFilterBreached();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetFilterSortBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupSortChips();
        setupCategoryChips();
        setupStrengthChips();
        setupAdditionalFilters();
        setupButtons();
        
        // Restore current selections
        restoreSelections();
    }

    private void setupSortChips() {
        binding.chipGroupSort.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipSortAZ) {
                currentSort = SortOption.A_TO_Z;
            } else if (checkedId == R.id.chipSortZA) {
                currentSort = SortOption.Z_TO_A;
            } else if (checkedId == R.id.chipSortRecent) {
                currentSort = SortOption.RECENT;
            } else if (checkedId == R.id.chipSortOldest) {
                currentSort = SortOption.OLDEST;
            } else if (checkedId == R.id.chipSortStrength) {
                currentSort = SortOption.STRENGTH;
            }
        });
    }

    private void setupCategoryChips() {
        binding.chipGroupCategory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.get(0) == R.id.chipCategoryAll) {
                currentCategory = null;
            } else {
                int checkedId = checkedIds.get(0);
                Chip chip = group.findViewById(checkedId);
                if (chip != null) {
                    currentCategory = getCategoryFromChip(checkedId);
                }
            }
        });
    }

    private String getCategoryFromChip(int chipId) {
        if (chipId == R.id.chipCategorySocial) return "Social Media";
        if (chipId == R.id.chipCategoryEmail) return "Email";
        if (chipId == R.id.chipCategoryBanking) return "Banking";
        if (chipId == R.id.chipCategoryShopping) return "Shopping";
        if (chipId == R.id.chipCategoryWork) return "Work";
        if (chipId == R.id.chipCategoryOther) return "Other";
        return null;
    }

    private int getChipFromCategory(String category) {
        if (category == null) return R.id.chipCategoryAll;
        switch (category) {
            case "Social Media": return R.id.chipCategorySocial;
            case "Email": return R.id.chipCategoryEmail;
            case "Banking": return R.id.chipCategoryBanking;
            case "Shopping": return R.id.chipCategoryShopping;
            case "Work": return R.id.chipCategoryWork;
            case "Other": return R.id.chipCategoryOther;
            default: return R.id.chipCategoryAll;
        }
    }

    private void setupStrengthChips() {
        binding.chipGroupStrength.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty() || checkedIds.get(0) == R.id.chipStrengthAll) {
                currentStrength = StrengthFilter.ALL;
            } else {
                int checkedId = checkedIds.get(0);
                if (checkedId == R.id.chipStrengthWeak) {
                    currentStrength = StrengthFilter.WEAK;
                } else if (checkedId == R.id.chipStrengthFair) {
                    currentStrength = StrengthFilter.FAIR;
                } else if (checkedId == R.id.chipStrengthGood) {
                    currentStrength = StrengthFilter.GOOD;
                } else if (checkedId == R.id.chipStrengthStrong) {
                    currentStrength = StrengthFilter.STRONG;
                }
            }
        });
    }

    private void setupAdditionalFilters() {
        binding.chipOldPasswords.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterOldPasswords = isChecked;
        });

        binding.chipReused.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterReused = isChecked;
        });

        binding.chipBreached.setOnCheckedChangeListener((buttonView, isChecked) -> {
            filterBreached = isChecked;
        });
    }

    private void setupButtons() {
        binding.btnReset.setOnClickListener(v -> resetFilters());
        binding.btnApply.setOnClickListener(v -> applyFilters());
    }

    private void restoreSelections() {
        // Restore sort selection
        switch (currentSort) {
            case A_TO_Z:
                binding.chipSortAZ.setChecked(true);
                break;
            case Z_TO_A:
                binding.chipSortZA.setChecked(true);
                break;
            case RECENT:
                binding.chipSortRecent.setChecked(true);
                break;
            case OLDEST:
                binding.chipSortOldest.setChecked(true);
                break;
            case STRENGTH:
                binding.chipSortStrength.setChecked(true);
                break;
        }

        // Restore category selection
        binding.chipGroupCategory.check(getChipFromCategory(currentCategory));

        // Restore strength selection
        switch (currentStrength) {
            case ALL:
                binding.chipStrengthAll.setChecked(true);
                break;
            case WEAK:
                binding.chipStrengthWeak.setChecked(true);
                break;
            case FAIR:
                binding.chipStrengthFair.setChecked(true);
                break;
            case GOOD:
                binding.chipStrengthGood.setChecked(true);
                break;
            case STRONG:
                binding.chipStrengthStrong.setChecked(true);
                break;
        }

        // Restore additional filters
        binding.chipOldPasswords.setChecked(filterOldPasswords);
        binding.chipReused.setChecked(filterReused);
        binding.chipBreached.setChecked(filterBreached);
    }

    private void resetFilters() {
        currentSort = SortOption.RECENT;
        currentCategory = null;
        currentStrength = StrengthFilter.ALL;
        filterOldPasswords = false;
        filterReused = false;
        filterBreached = false;
        restoreSelections();
    }

    private void applyFilters() {
        if (listener != null) {
            FilterSortOptions options = new FilterSortOptions(
                    currentSort,
                    currentCategory,
                    currentStrength,
                    filterOldPasswords,
                    filterReused,
                    filterBreached
            );
            listener.onFilterSortApplied(options);
        }
        dismiss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // ==================== ENUMS ====================

    public enum SortOption {
        A_TO_Z,
        Z_TO_A,
        RECENT,
        OLDEST,
        STRENGTH
    }

    public enum StrengthFilter {
        ALL,
        WEAK,      // < 30
        FAIR,      // 30-50
        GOOD,      // 50-70
        STRONG     // > 70
    }

    // ==================== DATA CLASS ====================

    public static class FilterSortOptions implements java.io.Serializable {
        private final SortOption sortOption;
        private final String category;
        private final StrengthFilter strengthFilter;
        private final boolean filterOldPasswords;
        private final boolean filterReused;
        private final boolean filterBreached;

        public FilterSortOptions(SortOption sortOption, String category, 
                                  StrengthFilter strengthFilter, boolean filterOldPasswords,
                                  boolean filterReused, boolean filterBreached) {
            this.sortOption = sortOption;
            this.category = category;
            this.strengthFilter = strengthFilter;
            this.filterOldPasswords = filterOldPasswords;
            this.filterReused = filterReused;
            this.filterBreached = filterBreached;
        }

        public SortOption getSortOption() { return sortOption; }
        public String getCategory() { return category; }
        public StrengthFilter getStrengthFilter() { return strengthFilter; }
        public boolean isFilterOldPasswords() { return filterOldPasswords; }
        public boolean isFilterReused() { return filterReused; }
        public boolean isFilterBreached() { return filterBreached; }

        public boolean hasActiveFilters() {
            return category != null || 
                   strengthFilter != StrengthFilter.ALL ||
                   filterOldPasswords || filterReused || filterBreached;
        }

        public static FilterSortOptions getDefault() {
            return new FilterSortOptions(
                    SortOption.RECENT, null, StrengthFilter.ALL,
                    false, false, false
            );
        }
    }
}
