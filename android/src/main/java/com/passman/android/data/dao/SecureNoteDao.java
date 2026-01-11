package com.passman.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.passman.android.data.entity.SecureNoteEntity;

import java.util.List;

/**
 * Data Access Object for secure notes
 */
@Dao
public interface SecureNoteDao {

    @Insert
    long insert(SecureNoteEntity note);

    @Update
    void update(SecureNoteEntity note);

    @Delete
    void delete(SecureNoteEntity note);

    @Query("DELETE FROM secure_notes WHERE id = :noteId")
    void deleteById(long noteId);

    @Query("SELECT * FROM secure_notes ORDER BY is_pinned DESC, updated_at DESC")
    LiveData<List<SecureNoteEntity>> getAllNotes();

    @Query("SELECT * FROM secure_notes ORDER BY is_pinned DESC, updated_at DESC")
    List<SecureNoteEntity> getAllNotesSync();

    @Query("SELECT * FROM secure_notes WHERE id = :noteId")
    LiveData<SecureNoteEntity> getNoteById(long noteId);

    @Query("SELECT * FROM secure_notes WHERE id = :noteId")
    SecureNoteEntity getNoteByIdSync(long noteId);

    @Query("SELECT * FROM secure_notes WHERE is_favorite = 1 ORDER BY updated_at DESC")
    LiveData<List<SecureNoteEntity>> getFavoriteNotes();

    @Query("SELECT * FROM secure_notes WHERE category = :category ORDER BY updated_at DESC")
    LiveData<List<SecureNoteEntity>> getNotesByCategory(String category);

    @Query("SELECT * FROM secure_notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    LiveData<List<SecureNoteEntity>> searchNotes(String query);

    @Query("SELECT DISTINCT category FROM secure_notes WHERE category IS NOT NULL AND category != ''")
    LiveData<List<String>> getAllCategories();

    @Query("SELECT COUNT(*) FROM secure_notes")
    int getNoteCount();

    @Query("UPDATE secure_notes SET is_favorite = :isFavorite WHERE id = :noteId")
    void setFavorite(long noteId, boolean isFavorite);

    @Query("UPDATE secure_notes SET is_pinned = :isPinned WHERE id = :noteId")
    void setPinned(long noteId, boolean isPinned);
}
