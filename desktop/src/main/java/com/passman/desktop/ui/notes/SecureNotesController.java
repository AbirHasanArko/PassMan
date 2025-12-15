package com.passman.desktop.ui.notes;

import com.passman.core.db.DatabaseManager;
import com. passman.core.model.SecureNote;
import com.passman. core.repository.SecureNotesRepositoryImpl;
import com.passman.core.services.SecureNotesService;
import com.passman. desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx. stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalDateTime;
import java. util.List;

/**
 * Controller for Secure Notes
 */
public class SecureNotesController {

    @FXML private ListView<SecureNote> notesListView;
    @FXML private TextField searchField;
    @FXML private TextField titleField;
    @FXML private TextArea contentArea;
    @FXML private ComboBox<SecureNote.NoteCategory> categoryComboBox;
    @FXML private TextField tagsField;
    @FXML private CheckBox favoriteCheckbox;
    @FXML private ColorPicker colorPicker;
    @FXML private ListView<String> attachmentsListView;
    @FXML private Label statusLabel;

    private SecureNotesViewModel viewModel;
    private SecureNotesService notesService;
    private SecureNote currentNote;

    @FXML
    public void initialize() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        SecureNotesRepositoryImpl repository = new SecureNotesRepositoryImpl(dbManager);

        String storagePath = System.getProperty("user.home") + "/.passman";
        notesService = new SecureNotesService(repository, storagePath);

        viewModel = new SecureNotesViewModel(notesService);

        categoryComboBox.setItems(FXCollections.observableArrayList(SecureNote.NoteCategory.values()));
        categoryComboBox. setValue(SecureNote.NoteCategory.PERSONAL);

        notesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(SecureNote note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                } else {
                    setText(note.getTitle());
                }
            }
        });

        notesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                loadNote(newVal);
            }
        });

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            performSearch(newVal);
        });

        loadAllNotes();
    }

    private void loadAllNotes() {
        try {
            List<SecureNote> notes = notesService.getAllNotes();
            ObservableList<SecureNote> observableNotes = FXCollections.observableArrayList(notes);
            notesListView.setItems(observableNotes);
            statusLabel.setText(notes.size() + " notes loaded");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load notes", e.getMessage());
        }
    }

    private void performSearch(String query) {
        if (query == null || query.trim().isEmpty()) {
            loadAllNotes();
            return;
        }

        try {
            List<SecureNote> results = notesService.searchNotes(query);
            ObservableList<SecureNote> observableResults = FXCollections.observableArrayList(results);
            notesListView.setItems(observableResults);
            statusLabel.setText(results.size() + " notes found");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Search failed", e.getMessage());
        }
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
        SecureNote.NoteCategory category = categoryComboBox.getValue();
        if (category != null) {
            try {
                List<SecureNote> filtered = notesService.getNotesByCategory(category);
                ObservableList<SecureNote> observableFiltered = FXCollections.observableArrayList(filtered);
                notesListView.setItems(observableFiltered);
                statusLabel.setText(filtered.size() + " notes in " + category.getDisplayName());
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to filter notes", e.getMessage());
            }
        }
    }

    @FXML
    private void handleShowFavorites() {
        try {
            List<SecureNote> favorites = notesService.getFavoriteNotes();
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