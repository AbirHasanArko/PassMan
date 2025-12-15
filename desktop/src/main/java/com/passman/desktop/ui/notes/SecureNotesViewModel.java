package com.passman.desktop.ui.notes;

import com.passman.core. model.SecureNote;
import com.passman.core.services. SecureNotesService;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ViewModel for Secure Notes
 */
public class SecureNotesViewModel {

    private final SecureNotesService notesService;
    private final ObservableList<SecureNote> notes = FXCollections.observableArrayList();
    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final ObjectProperty<SecureNote> selectedNote = new SimpleObjectProperty<>();

    public SecureNotesViewModel(SecureNotesService notesService) {
        this.notesService = notesService;
    }

    public void loadNotes() {
        try {
            notes.clear();
            notes.addAll(notesService.getAllNotes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObservableList<SecureNote> getNotes() {
        return notes;
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    public ObjectProperty<SecureNote> selectedNoteProperty() {
        return selectedNote;
    }
}