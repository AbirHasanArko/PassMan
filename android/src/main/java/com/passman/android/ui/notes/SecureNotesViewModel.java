package com.passman.android.ui.notes;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.passman.android.data.entity.SecureNoteEntity;
import com.passman.android.data.repository.SecureNoteRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for managing secure notes
 */
public class SecureNotesViewModel extends AndroidViewModel {

    private final SecureNoteRepository repository;
    
    private final MutableLiveData<SecureNoteEntity> currentNote = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> filterCategory = new MutableLiveData<>(null);
    
    private final MediatorLiveData<List<SecureNoteEntity>> filteredNotes = new MediatorLiveData<>();
    private List<SecureNoteEntity> allNotesCache = new ArrayList<>();

    public SecureNotesViewModel(@NonNull Application application) {
        super(application);
        this.repository = new SecureNoteRepository(application);
        setupFilteredNotes();
    }

    private void setupFilteredNotes() {
        // Add source for all notes
        filteredNotes.addSource(repository.getAllNotes(), notes -> {
            allNotesCache = notes != null ? notes : new ArrayList<>();
            applyFilters();
        });
        
        // Add source for search query changes
        filteredNotes.addSource(searchQuery, query -> applyFilters());
        
        // Add source for category filter changes
        filteredNotes.addSource(filterCategory, category -> applyFilters());
    }

    private void applyFilters() {
        String query = searchQuery.getValue();
        String category = filterCategory.getValue();
        
        List<SecureNoteEntity> result = new ArrayList<>();
        
        for (SecureNoteEntity note : allNotesCache) {
            boolean matchesQuery = true;
            boolean matchesCategory = true;
            
            // Check search query
            if (query != null && !query.isEmpty()) {
                String lowerQuery = query.toLowerCase();
                matchesQuery = (note.getTitle() != null && note.getTitle().toLowerCase().contains(lowerQuery)) ||
                              (note.getContent() != null && note.getContent().toLowerCase().contains(lowerQuery)) ||
                              (note.getTags() != null && note.getTags().toLowerCase().contains(lowerQuery));
            }
            
            // Check category
            if (category != null && !category.isEmpty()) {
                matchesCategory = category.equals(note.getCategory());
            }
            
            if (matchesQuery && matchesCategory) {
                result.add(note);
            }
        }
        
        filteredNotes.setValue(result);
    }

    // ========== Notes List ==========

    public LiveData<List<SecureNoteEntity>> getAllNotes() {
        return repository.getAllNotes();
    }

    public LiveData<List<SecureNoteEntity>> getFilteredNotes() {
        return filteredNotes;
    }

    public LiveData<List<SecureNoteEntity>> getFavoriteNotes() {
        return repository.getFavoriteNotes();
    }

    public LiveData<List<SecureNoteEntity>> getNotesByCategory(String category) {
        return repository.getNotesByCategory(category);
    }

    public LiveData<List<SecureNoteEntity>> searchNotes(String query) {
        return repository.searchNotes(query);
    }

    public LiveData<List<String>> getAllCategories() {
        return repository.getAllCategories();
    }

    public void refreshNotes() {
        // Force refresh by re-observing
        applyFilters();
    }

    // ========== Current Note ==========

    public void loadNote(long noteId) {
        isLoading.setValue(true);
        repository.getNoteById(noteId).observeForever(note -> {
            currentNote.setValue(note);
            isLoading.setValue(false);
        });
    }

    public LiveData<SecureNoteEntity> getCurrentNote() {
        return currentNote;
    }

    // ========== CRUD ==========

    public void saveNote(SecureNoteEntity note) {
        isLoading.setValue(true);
        
        long currentTime = System.currentTimeMillis();
        note.setUpdatedAt(currentTime);
        
        if (note.getId() == 0) {
            // New note
            note.setCreatedAt(currentTime);
            repository.insert(note, noteId -> {
                successMessage.postValue("Note saved");
                isLoading.postValue(false);
            });
        } else {
            // Update existing
            repository.update(note);
            successMessage.postValue("Note updated");
            isLoading.postValue(false);
        }
    }

    public void deleteNote(SecureNoteEntity note) {
        deleteNote(note.getId());
    }

    public void deleteNote(long noteId) {
        isLoading.setValue(true);
        repository.delete(noteId);
        successMessage.postValue("Note deleted");
        isLoading.postValue(false);
    }

    public void toggleFavorite(SecureNoteEntity note) {
        repository.setFavorite(note.getId(), !note.isFavorite());
    }

    public void togglePinned(SecureNoteEntity note) {
        repository.setPinned(note.getId(), !note.isPinned());
    }

    // ========== Filter State ==========

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void setFilterCategory(String category) {
        filterCategory.setValue(category);
    }

    public LiveData<String> getFilterCategory() {
        return filterCategory;
    }

    // ========== UI State ==========

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }
}
