package com.passman.android.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.passman.android.data.dao.SecureNoteDao;
import com.passman.android.data.database.PassManDatabase;
import com.passman.android.data.entity.SecureNoteEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for secure note operations
 */
public class SecureNoteRepository {

    private final SecureNoteDao noteDao;
    private final ExecutorService executorService;

    public SecureNoteRepository(Application application) {
        PassManDatabase database = PassManDatabase.getInstance(application);
        this.noteDao = database.secureNoteDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<SecureNoteEntity>> getAllNotes() {
        return noteDao.getAllNotes();
    }

    public List<SecureNoteEntity> getAllNotesSync() {
        return noteDao.getAllNotesSync();
    }

    public LiveData<SecureNoteEntity> getNoteById(long noteId) {
        return noteDao.getNoteById(noteId);
    }

    public SecureNoteEntity getNoteByIdSync(long noteId) {
        return noteDao.getNoteByIdSync(noteId);
    }

    public LiveData<List<SecureNoteEntity>> getFavoriteNotes() {
        return noteDao.getFavoriteNotes();
    }

    public LiveData<List<SecureNoteEntity>> getNotesByCategory(String category) {
        return noteDao.getNotesByCategory(category);
    }

    public LiveData<List<SecureNoteEntity>> searchNotes(String query) {
        return noteDao.searchNotes(query);
    }

    public LiveData<List<String>> getAllCategories() {
        return noteDao.getAllCategories();
    }

    public void insert(SecureNoteEntity note, OnNoteInsertedCallback callback) {
        executorService.execute(() -> {
            long id = noteDao.insert(note);
            if (callback != null) {
                callback.onNoteInserted(id);
            }
        });
    }

    public void update(SecureNoteEntity note) {
        executorService.execute(() -> noteDao.update(note));
    }

    public void delete(long noteId) {
        executorService.execute(() -> noteDao.deleteById(noteId));
    }

    public void setFavorite(long noteId, boolean isFavorite) {
        executorService.execute(() -> noteDao.setFavorite(noteId, isFavorite));
    }

    public void setPinned(long noteId, boolean isPinned) {
        executorService.execute(() -> noteDao.setPinned(noteId, isPinned));
    }

    public interface OnNoteInsertedCallback {
        void onNoteInserted(long noteId);
    }
}
