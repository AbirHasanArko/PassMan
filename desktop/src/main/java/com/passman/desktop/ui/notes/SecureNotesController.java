package com.passman.desktop.ui.notes;

import com.passman.core.db.DatabaseManager;
import com.passman.core.model.SecureNote;
import com.passman.core.repository.SecureNotesRepositoryImpl;
import com.passman.core.services.SecureNotesService;
import com.passman.desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for Secure Notes with advanced search and filtering
 */
public class SecureNotesController {

    // Search Scope enum
    public enum SearchScope {
        TITLE_ONLY("Title Only"),
        CONTENT_ONLY("Content Only"),
        TAGS_ONLY("Tags Only"),
        BOTH("Title & Content"),
        ALL("All Fields");

        private final String displayName;
        SearchScope(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    // Sort Options enum
    public enum NoteSortOption {
        TITLE_ASC("Title (A-Z)"),
        TITLE_DESC("Title (Z-A)"),
        RECENT("Recently Modified"),
        OLDEST("Oldest First"),
        CREATED_RECENT("Recently Created"),
        CREATED_OLDEST("Created Oldest");

        private final String displayName;
        NoteSortOption(String displayName) { this.displayName = displayName; }
        public String getDisplayName() { return displayName; }
        @Override public String toString() { return displayName; }
    }

    @FXML private ListView<SecureNote> notesListView;
    @FXML private TextField searchField;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<SecureNote.NoteCategory> categoryComboBox;
    @FXML private ComboBox<SecureNote.NoteCategory> categoryFilterComboBox;
    @FXML private TextField tagsField;
    @FXML private TextField tagFilterField;
    @FXML private CheckBox favoriteCheckbox;
    @FXML private ColorPicker colorPicker;
    @FXML private ListView<String> attachmentsListView;
    @FXML private Label statusLabel;

    // Advanced filter controls
    @FXML private ComboBox<SearchScope> searchScopeCombo;
    @FXML private ComboBox<NoteSortOption> sortCombo;
    @FXML private CheckBox hasAttachmentsCheck;
    @FXML private CheckBox hasTagsCheck;

    private SecureNotesViewModel viewModel;
    private SecureNotesService notesService;
    private SecureNote currentNote;
    private List<SecureNote> allNotes;

    @FXML
    public void initialize() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        SecureNotesRepositoryImpl repository = new SecureNotesRepositoryImpl(dbManager);

        String storagePath = System.getProperty("user.home") + "/.passman";
        notesService = new SecureNotesService(repository, storagePath);

        viewModel = new SecureNotesViewModel(notesService);

        // Setup category combo for editor
        categoryComboBox.setItems(FXCollections.observableArrayList(SecureNote.NoteCategory.values()));
        categoryComboBox.setValue(SecureNote.NoteCategory.PERSONAL);

        // Setup category filter combo with "All" option
        ObservableList<SecureNote.NoteCategory> categoryFilterItems = FXCollections.observableArrayList();
        categoryFilterItems.add(null); // null represents "All Categories"
        categoryFilterItems.addAll(SecureNote.NoteCategory.values());
        categoryFilterComboBox.setItems(categoryFilterItems);
        categoryFilterComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(SecureNote.NoteCategory item, boolean empty) {
                super.updateItem(item, empty);
                setText(item == null ? "All Categories" : item.getDisplayName());
            }
        });
        categoryFilterComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SecureNote.NoteCategory item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                } else {
                    setText(item == null ? "All Categories" : item.getDisplayName());
                }
            }
        });

        // Setup search scope combo
        searchScopeCombo.setItems(FXCollections.observableArrayList(SearchScope.values()));
        searchScopeCombo.setValue(SearchScope.ALL);

        // Setup sort combo
        sortCombo.setItems(FXCollections.observableArrayList(NoteSortOption.values()));
        sortCombo.setValue(NoteSortOption.RECENT);

        // Setup notes list view
        notesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SecureNote note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                } else {
                    String prefix = "";
                    if (note.isFavorite()) prefix += "⭐ ";
                    if (note.isHasAttachments()) prefix += "📎 ";
                    setText(prefix + note.getTitle());
                }
            }
        });

        notesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadNote(newVal);
            }
        });

        // Setup search field listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFiltersAndSort();
        });

        // Setup tag filter listener
        tagFilterField.textProperty().addListener((obs, oldVal, newVal) -> {
            applyFiltersAndSort();
        });

        loadAllNotes();
    }

    private void loadAllNotes() {
        try {
            allNotes = notesService.getAllNotes();
            applyFiltersAndSort();
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load notes", e.getMessage());
        }
    }

    private void applyFiltersAndSort() {
        if (allNotes == null) return;

        String searchQuery = searchField.getText();
        SearchScope scope = searchScopeCombo.getValue();
        SecureNote.NoteCategory selectedCategory = categoryFilterComboBox.getValue();
        String tagFilter = tagFilterField.getText();
        boolean filterHasAttachments = hasAttachmentsCheck.isSelected();
        boolean filterHasTags = hasTagsCheck.isSelected();
        NoteSortOption sortOption = sortCombo.getValue();

        List<SecureNote> filtered = allNotes.stream()
                // Search filter based on scope
                .filter(note -> {
                    if (searchQuery == null || searchQuery.trim().isEmpty()) return true;
                    String lowerQuery = searchQuery.toLowerCase();
                    boolean matchesTitle = note.getTitle() != null &&
                            note.getTitle().toLowerCase().contains(lowerQuery);
                    boolean matchesContent = note.getContent() != null &&
                            note.getContent().toLowerCase().contains(lowerQuery);
                    boolean matchesTags = note.getTags() != null &&
                            note.getTags().toLowerCase().contains(lowerQuery);

                    if (scope == null || scope == SearchScope.ALL) {
                        return matchesTitle || matchesContent || matchesTags;
                    } else if (scope == SearchScope.BOTH) {
                        return matchesTitle || matchesContent;
                    } else if (scope == SearchScope.TITLE_ONLY) {
                        return matchesTitle;
                    } else if (scope == SearchScope.CONTENT_ONLY) {
                        return matchesContent;
                    } else if (scope == SearchScope.TAGS_ONLY) {
                        return matchesTags;
                    }
                    return true;
                })
                // Category filter
                .filter(note -> selectedCategory == null || note.getCategory() == selectedCategory)
                // Tag filter
                .filter(note -> {
                    if (tagFilter == null || tagFilter.trim().isEmpty()) return true;
                    return note.getTags() != null &&
                            note.getTags().toLowerCase().contains(tagFilter.toLowerCase());
                })
                // Has attachments filter
                .filter(note -> !filterHasAttachments || note.isHasAttachments())
                // Has tags filter
                .filter(note -> !filterHasTags || (note.getTags() != null && !note.getTags().trim().isEmpty()))
                .collect(Collectors.toList());

        // Apply sorting
        if (sortOption != null) {
            Comparator<SecureNote> comparator = switch (sortOption) {
                case TITLE_ASC -> Comparator.comparing(SecureNote::getTitle,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                case TITLE_DESC -> Comparator.comparing(SecureNote::getTitle,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)).reversed();
                case RECENT -> Comparator.comparing(SecureNote::getLastModified,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                case OLDEST -> Comparator.comparing(SecureNote::getLastModified,
                        Comparator.nullsLast(Comparator.naturalOrder()));
                case CREATED_RECENT -> Comparator.comparing(SecureNote::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed();
                case CREATED_OLDEST -> Comparator.comparing(SecureNote::getCreatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder()));
            };
            filtered.sort(comparator);
        }

        ObservableList<SecureNote> observableNotes = FXCollections.observableArrayList(filtered);
        notesListView.setItems(observableNotes);
        statusLabel.setText(filtered.size() + " notes" + (filtered.size() != allNotes.size() ? " (filtered)" : ""));
    }

    @FXML
    private void handleSearchScopeChange() {
        applyFiltersAndSort();
    }

    @FXML
    private void handleAdvancedFilterChange() {
        applyFiltersAndSort();
    }

    @FXML
    private void handleSortChange() {
        applyFiltersAndSort();
    }

    @FXML
    private void handleClearFilters() {
        searchField.clear();
        searchScopeCombo.setValue(SearchScope.ALL);
        categoryFilterComboBox.setValue(null);
        tagFilterField.clear();
        hasAttachmentsCheck.setSelected(false);
        hasTagsCheck.setSelected(false);
        sortCombo.setValue(NoteSortOption.RECENT);
        applyFiltersAndSort();
    }

    @FXML
    private void handleShowAll() {
        handleClearFilters();
    }

    private void performSearch(String query) {
        applyFiltersAndSort();
    }

    private void loadNote(SecureNote note) {
        try {
            var fullNote = notesService.getNote(note.getId(), SessionManager.getInstance().getMasterKey());
            if (fullNote. isPresent()) {
                currentNote = fullNote.get();
                titleField.setText(currentNote. getTitle());
                contentArea. setText(currentNote.getContent());
                categoryComboBox.setValue(currentNote.getCategory());
                tagsField.setText(currentNote.getTags());
                favoriteCheckbox.setSelected(currentNote.isFavorite());

                if (currentNote.getColorCode() != null) {
                    colorPicker.setValue(javafx.scene.paint.Color. web(currentNote.getColorCode()));
                }

                loadAttachments();
            }
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load note", e.getMessage());
        }
    }

    private void loadAttachments() {
        ObservableList<String> attachmentNames = FXCollections.observableArrayList();
        if (currentNote != null && currentNote.getAttachments() != null) {
            currentNote.getAttachments().forEach(att ->
                    attachmentNames.add(att.getOriginalFileName())
            );
        }
        attachmentsListView.setItems(attachmentNames);
    }

    @FXML
    private void handleNewNote() {
        currentNote = null;
        titleField.clear();
        contentArea.clear();
        categoryComboBox.setValue(SecureNote.NoteCategory. PERSONAL);
        tagsField.clear();
        favoriteCheckbox.setSelected(false);
        colorPicker.setValue(javafx.scene.paint.Color. LIGHTBLUE);
        attachmentsListView.getItems().clear();
    }

    @FXML
    private void handleSaveNote() {
        if (titleField.getText().isEmpty()) {
            DialogUtils.showWarning("Validation", "Title Required", "Please enter a note title");
            return;
        }

        try {
            if (currentNote == null) {
                currentNote = new SecureNote();
                currentNote.setCreatedAt(LocalDateTime.now());
            }

            currentNote. setTitle(titleField.getText());
            currentNote.setContent(contentArea.getText());
            currentNote.setCategory(categoryComboBox.getValue());
            currentNote.setTags(tagsField.getText());
            currentNote. setFavorite(favoriteCheckbox.isSelected());
            currentNote.setColorCode(colorPicker.getValue().toString());
            currentNote.setLastModified(LocalDateTime.now());

            notesService.saveNote(currentNote, SessionManager.getInstance().getMasterKey());

            DialogUtils.showInfo("Success", "Note Saved", "Your note has been saved successfully");
            loadAllNotes();

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to save note", e.getMessage());
        }
    }

    @FXML
    private void handleDeleteNote() {
        if (currentNote == null) {
            DialogUtils.showWarning("No Selection", "No Note Selected", "Please select a note to delete");
            return;
        }

        boolean confirm = DialogUtils.showConfirmation(
                "Delete Note",
                "Are you sure? ",
                "This will permanently delete the note and all attachments."
        );

        if (confirm) {
            try {
                notesService. deleteNote(currentNote.getId());
                handleNewNote();
                loadAllNotes();
                DialogUtils.showInfo("Success", "Note Deleted", "The note has been deleted");
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to delete note", e.getMessage());
            }
        }
    }

    @FXML
    private void handleAddAttachment() {
        if (currentNote == null || currentNote.getId() == null) {
            DialogUtils.showWarning("Save First", "Save Note First", "Please save the note before adding attachments");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select File to Attach");
        Stage stage = (Stage) titleField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                notesService.addAttachment(
                        currentNote.getId(),
                        file,
                        SessionManager.getInstance().getMasterKey()
                );
                loadNote(currentNote);
                DialogUtils.showInfo("Success", "Attachment Added", "File attached successfully");
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to add attachment", e.getMessage());
            }
        }
    }

    @FXML
    private void handleDownloadAttachment() {
        String selectedAttachment = attachmentsListView.getSelectionModel().getSelectedItem();
        if (selectedAttachment == null) {
            DialogUtils. showWarning("No Selection", "No Attachment Selected", "Please select an attachment to download");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Attachment As");
        fileChooser.setInitialFileName(selectedAttachment);
        Stage stage = (Stage) titleField.getScene().getWindow();
        File destinationFile = fileChooser. showSaveDialog(stage);

        if (destinationFile != null) {
            try {
                var attachment = currentNote.getAttachments().stream()
                        .filter(a -> a.getOriginalFileName().equals(selectedAttachment))
                        .findFirst();

                if (attachment. isPresent()) {
                    notesService.downloadAttachment(
                            attachment.get(),
                            destinationFile,
                            SessionManager.getInstance().getMasterKey()
                    );
                    DialogUtils.showInfo("Success", "Download Complete", "Attachment saved successfully");
                }
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to download attachment", e.getMessage());
            }
        }
    }

    @FXML
    private void handleFilterByCategory() {
        // Category filter is already bound through applyFiltersAndSort
        applyFiltersAndSort();
    }

    @FXML
    private void handleShowFavorites() {
        try {
            // Filter to show only favorites
            if (allNotes == null) return;

            List<SecureNote> favorites = allNotes.stream()
                    .filter(SecureNote::isFavorite)
                    .collect(Collectors.toList());

            ObservableList<SecureNote> observableFavorites = FXCollections.observableArrayList(favorites);
            notesListView.setItems(observableFavorites);
            statusLabel.setText(favorites.size() + " favorite notes");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load favorites", e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }
}