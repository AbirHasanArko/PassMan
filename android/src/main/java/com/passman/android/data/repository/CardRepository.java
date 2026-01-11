package com.passman.android.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.passman.android.data.dao.CardDao;
import com.passman.android.data.database.PassManDatabase;
import com.passman.android.data.entity.CardEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Repository for card operations
 */
public class CardRepository {

    private final CardDao cardDao;
    private final ExecutorService executorService;

    public CardRepository(Application application) {
        PassManDatabase database = PassManDatabase.getInstance(application);
        this.cardDao = database.cardDao();
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<CardEntity>> getAllCards() {
        return cardDao.getAllCards();
    }

    public List<CardEntity> getAllCardsSync() {
        return cardDao.getAllCardsSync();
    }

    public LiveData<CardEntity> getCardById(long cardId) {
        return cardDao.getCardById(cardId);
    }

    public CardEntity getCardByIdSync(long cardId) {
        return cardDao.getCardByIdSync(cardId);
    }

    public LiveData<List<CardEntity>> getCardsByType(String cardType) {
        return cardDao.getCardsByType(cardType);
    }

    public LiveData<List<CardEntity>> getFavoriteCards() {
        return cardDao.getFavoriteCards();
    }

    public LiveData<List<CardEntity>> searchCards(String query) {
        return cardDao.searchCards(query);
    }

    public LiveData<List<String>> getAllCardTypes() {
        return cardDao.getAllCardTypes();
    }

    public List<CardEntity> getCardsWithExpirySync() {
        return cardDao.getCardsWithExpirySync();
    }

    public void insert(CardEntity card, OnCardInsertedCallback callback) {
        executorService.execute(() -> {
            long id = cardDao.insert(card);
            if (callback != null) {
                callback.onCardInserted(id);
            }
        });
    }

    public void update(CardEntity card) {
        executorService.execute(() -> cardDao.update(card));
    }

    public void delete(long cardId) {
        executorService.execute(() -> cardDao.deleteById(cardId));
    }

    public void setFavorite(long cardId, boolean isFavorite) {
        executorService.execute(() -> cardDao.setFavorite(cardId, isFavorite));
    }

    public void updateLastReminderSent(long cardId, long timestamp) {
        executorService.execute(() -> cardDao.updateLastReminderSent(cardId, timestamp));
    }

    public interface OnCardInsertedCallback {
        void onCardInserted(long cardId);
    }
}
