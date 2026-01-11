package com.passman.android.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.passman.android.data.entity.CardEntity;

import java.util.List;

/**
 * Data Access Object for cards
 */
@Dao
public interface CardDao {

    @Insert
    long insert(CardEntity card);

    @Update
    void update(CardEntity card);

    @Delete
    void delete(CardEntity card);

    @Query("DELETE FROM cards WHERE id = :cardId")
    void deleteById(long cardId);

    @Query("SELECT * FROM cards ORDER BY is_favorite DESC, updated_at DESC")
    LiveData<List<CardEntity>> getAllCards();

    @Query("SELECT * FROM cards ORDER BY is_favorite DESC, updated_at DESC")
    List<CardEntity> getAllCardsSync();

    @Query("SELECT * FROM cards WHERE id = :cardId")
    LiveData<CardEntity> getCardById(long cardId);

    @Query("SELECT * FROM cards WHERE id = :cardId")
    CardEntity getCardByIdSync(long cardId);

    @Query("SELECT * FROM cards WHERE card_type = :cardType ORDER BY updated_at DESC")
    LiveData<List<CardEntity>> getCardsByType(String cardType);

    @Query("SELECT * FROM cards WHERE is_favorite = 1 ORDER BY updated_at DESC")
    LiveData<List<CardEntity>> getFavoriteCards();

    @Query("SELECT * FROM cards WHERE card_name LIKE '%' || :query || '%' OR cardholder_name LIKE '%' || :query || '%' OR issuer LIKE '%' || :query || '%' ORDER BY updated_at DESC")
    LiveData<List<CardEntity>> searchCards(String query);

    @Query("SELECT COUNT(*) FROM cards")
    int getCardCount();

    @Query("UPDATE cards SET is_favorite = :isFavorite WHERE id = :cardId")
    void setFavorite(long cardId, boolean isFavorite);

    @Query("UPDATE cards SET last_reminder_sent = :timestamp WHERE id = :cardId")
    void updateLastReminderSent(long cardId, long timestamp);

    // Get cards expiring within a date range for notifications
    @Query("SELECT * FROM cards WHERE expiry_year > 0 AND expiry_month > 0 ORDER BY expiry_year, expiry_month")
    List<CardEntity> getCardsWithExpirySync();

    @Query("SELECT DISTINCT card_type FROM cards")
    LiveData<List<String>> getAllCardTypes();
}
