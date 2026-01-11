package com.passman.android.ui.cards;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.passman.android.data.entity.CardEntity;
import com.passman.android.data.repository.CardRepository;
import com.passman.android.security.FileEncryptionManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ViewModel for managing cards
 */
public class CardsViewModel extends AndroidViewModel {

    private final CardRepository repository;
    private final FileEncryptionManager encryptionManager;
    private final ExecutorService executorService;
    
    private final MutableLiveData<CardEntity> currentCard = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> filterType = new MutableLiveData<>(null);
    
    private final MediatorLiveData<List<CardEntity>> filteredCards = new MediatorLiveData<>();
    private List<CardEntity> allCardsCache = new ArrayList<>();

    public CardsViewModel(@NonNull Application application) {
        super(application);
        this.repository = new CardRepository(application);
        this.encryptionManager = new FileEncryptionManager(application);
        this.executorService = Executors.newSingleThreadExecutor();
        setupFilteredCards();
    }

    private void setupFilteredCards() {
        // Add source for all cards
        filteredCards.addSource(repository.getAllCards(), cards -> {
            allCardsCache = cards != null ? cards : new ArrayList<>();
            applyFilters();
        });
        
        // Add source for search query changes
        filteredCards.addSource(searchQuery, query -> applyFilters());
        
        // Add source for type filter changes
        filteredCards.addSource(filterType, type -> applyFilters());
    }

    private void applyFilters() {
        String query = searchQuery.getValue();
        String type = filterType.getValue();
        
        List<CardEntity> result = new ArrayList<>();
        
        for (CardEntity card : allCardsCache) {
            boolean matchesQuery = true;
            boolean matchesType = true;
            
            // Check search query
            if (query != null && !query.isEmpty()) {
                String lowerQuery = query.toLowerCase();
                matchesQuery = (card.getCardName() != null && card.getCardName().toLowerCase().contains(lowerQuery)) ||
                              (card.getCardholderName() != null && card.getCardholderName().toLowerCase().contains(lowerQuery)) ||
                              (card.getIssuer() != null && card.getIssuer().toLowerCase().contains(lowerQuery));
            }
            
            // Check type filter
            if (type != null && !type.isEmpty()) {
                matchesType = type.equals(card.getCardType());
            }
            
            if (matchesQuery && matchesType) {
                result.add(card);
            }
        }
        
        filteredCards.setValue(result);
    }

    // ========== Cards List ==========

    public LiveData<List<CardEntity>> getAllCards() {
        return repository.getAllCards();
    }

    public LiveData<List<CardEntity>> getFilteredCards() {
        return filteredCards;
    }

    public LiveData<List<CardEntity>> getFavoriteCards() {
        return repository.getFavoriteCards();
    }

    public LiveData<List<CardEntity>> getCardsByType(String cardType) {
        return repository.getCardsByType(cardType);
    }

    public LiveData<List<CardEntity>> searchCards(String query) {
        return repository.searchCards(query);
    }

    public LiveData<List<String>> getAllCardTypes() {
        return repository.getAllCardTypes();
    }

    public void refreshCards() {
        applyFilters();
    }

    // ========== Current Card ==========

    public void loadCard(long cardId) {
        isLoading.setValue(true);
        repository.getCardById(cardId).observeForever(card -> {
            currentCard.setValue(card);
            isLoading.setValue(false);
        });
    }

    public LiveData<CardEntity> getCurrentCard() {
        return currentCard;
    }

    // ========== CRUD ==========

    public void saveCard(CardEntity card, SaveCallback callback) {
        isLoading.setValue(true);
        
        long currentTime = System.currentTimeMillis();
        card.setUpdatedAt(currentTime);
        
        if (card.getId() == 0) {
            // New card
            card.setCreatedAt(currentTime);
            repository.insert(card, cardId -> {
                successMessage.postValue("Card saved");
                isLoading.postValue(false);
                if (callback != null) callback.onSuccess(cardId);
            });
        } else {
            // Update existing
            repository.update(card);
            successMessage.postValue("Card updated");
            isLoading.postValue(false);
            if (callback != null) callback.onSuccess(card.getId());
        }
    }

    public void deleteCard(CardEntity card) {
        deleteCard(card.getId());
    }

    public void deleteCard(long cardId) {
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            // Get the card first to delete associated images
            CardEntity card = repository.getCardByIdSync(cardId);
            if (card != null) {
                // Delete front image if exists
                if (card.getFrontImagePath() != null) {
                    new File(card.getFrontImagePath()).delete();
                }
                // Delete back image if exists
                if (card.getBackImagePath() != null) {
                    new File(card.getBackImagePath()).delete();
                }
            }
            
            repository.delete(cardId);
            successMessage.postValue("Card deleted");
            isLoading.postValue(false);
        });
    }

    public void toggleFavorite(CardEntity card) {
        repository.setFavorite(card.getId(), !card.isFavorite());
    }

    // ========== Image Scanning ==========

    /**
     * Save a scanned image as encrypted PDF
     */
    public void saveScannedImage(Bitmap bitmap, long cardId, boolean isFront, ScanCallback callback) {
        isLoading.setValue(true);
        
        executorService.execute(() -> {
            try {
                // Create PDF from bitmap
                PdfDocument document = new PdfDocument();
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                        bitmap.getWidth(), bitmap.getHeight(), 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                page.getCanvas().drawBitmap(bitmap, 0, 0, null);
                document.finishPage(page);
                
                // Save to temp file
                File cardsDir = new File(getApplication().getFilesDir(), "cards");
                if (!cardsDir.exists()) cardsDir.mkdirs();
                
                String fileName = "card_" + cardId + "_" + (isFront ? "front" : "back") + "_" + System.currentTimeMillis() + ".pdf";
                File tempPdf = new File(cardsDir, fileName);
                
                try (FileOutputStream fos = new FileOutputStream(tempPdf)) {
                    document.writeTo(fos);
                }
                document.close();
                
                // Encrypt the PDF
                String encryptedPath = encryptionManager.encryptFileToPath(tempPdf, cardsDir);
                
                // Delete temp unencrypted file
                tempPdf.delete();
                
                // Update card with image path
                CardEntity card = repository.getCardByIdSync(cardId);
                if (card != null) {
                    if (isFront) {
                        // Delete old front image if exists
                        if (card.getFrontImagePath() != null) {
                            new File(card.getFrontImagePath()).delete();
                        }
                        card.setFrontImagePath(encryptedPath);
                    } else {
                        // Delete old back image if exists
                        if (card.getBackImagePath() != null) {
                            new File(card.getBackImagePath()).delete();
                        }
                        card.setBackImagePath(encryptedPath);
                    }
                    repository.update(card);
                }
                
                successMessage.postValue((isFront ? "Front" : "Back") + " image saved");
                isLoading.postValue(false);
                
                if (callback != null) {
                    callback.onSuccess(encryptedPath);
                }
            } catch (Exception e) {
                errorMessage.postValue("Failed to save image: " + e.getMessage());
                isLoading.postValue(false);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }

    // ========== Filter State ==========

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    public void setFilterType(String type) {
        filterType.setValue(type);
    }

    public LiveData<String> getFilterType() {
        return filterType;
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

    // ========== Callbacks ==========

    public interface SaveCallback {
        void onSuccess(long cardId);
        void onError(String error);
    }

    public interface ScanCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }
}
