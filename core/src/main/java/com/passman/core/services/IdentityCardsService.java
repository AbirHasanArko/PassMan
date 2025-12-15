package com.passman.core.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.passman.core.crypto.AESCipher;
import com.passman.core.crypto.CipherFactory;
import com.passman.core.model.IdentityCard;
import com.passman.core.repository.IdentityCardsRepository;

import javax.crypto.SecretKey;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for managing digital identity cards
 */
public class IdentityCardsService {

    private final IdentityCardsRepository cardsRepository;
    private final AESCipher aesCipher;
    private final Gson gson;

    public IdentityCardsService(IdentityCardsRepository cardsRepository) {
        this.cardsRepository = cardsRepository;
        this.aesCipher = CipherFactory.createAESCipher();
        this.gson = new Gson();
    }

    public IdentityCard saveCard(IdentityCard card, SecretKey masterKey) throws Exception {
        Map<String, String> cardData = card.getCardData();
        if (cardData != null && !cardData.isEmpty()) {
            String jsonData = gson.toJson(cardData);
            String encryptedData = aesCipher. encrypt(jsonData, masterKey);
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            byte[] iv = new byte[16];
            byte[] encrypted = new byte[combined.length - 16];
            System.arraycopy(combined, 0, iv, 0, 16);
            System.arraycopy(combined, 16, encrypted, 0, encrypted.length);

            card.setEncryptionIV(iv);
            card.setEncryptedData(encrypted);

            extractLast4Digits(card, cardData);
        }

        if (card.getEncryptedPhoto() != null) {
            byte[] photoData = card.getEncryptedPhoto();
            byte[] encryptedPhoto = aesCipher.encryptBytes(photoData, masterKey);

            byte[] photoIV = new byte[16];
            byte[] encryptedPhotoData = new byte[encryptedPhoto.length - 16];
            System.arraycopy(encryptedPhoto, 0, photoIV, 0, 16);
            System.arraycopy(encryptedPhoto, 16, encryptedPhotoData, 0, encryptedPhotoData.length);

            card.setPhotoEncryptionIV(photoIV);
            card.setEncryptedPhoto(encryptedPhotoData);
        }

        if (card.getExpiryDate() != null) {
            card.setExpired(card.getExpiryDate().isBefore(LocalDate.now()));
        }

        card.setLastModified(LocalDateTime.now());

        if (card.getId() == null) {
            return cardsRepository. save(card);
        } else {
            cardsRepository.update(card);
            return card;
        }
    }

    private void extractLast4Digits(IdentityCard card, Map<String, String> cardData) {
        String numberField = null;

        switch (card.getCardType()) {
            case PASSPORT:  numberField = cardData.get("passportNumber"); break;
            case DRIVERS_LICENSE: numberField = cardData.get("licenseNumber"); break;
            case NATIONAL_ID: numberField = cardData.get("idNumber"); break;
            case CREDIT_CARD:
            case DEBIT_CARD:  numberField = cardData.get("cardNumber"); break;
            case BANK_ACCOUNT: numberField = cardData.get("accountNumber"); break;
            case INSURANCE:  numberField = cardData.get("policyNumber"); break;
            case SSN: numberField = cardData. get("ssn"); break;
            case MEMBERSHIP:  numberField = cardData.get("memberNumber"); break;
        }

        if (numberField != null && numberField.length() >= 4) {
            card. setCardNumberLast4(numberField.substring(numberField. length() - 4));
        }
    }

    public Optional<IdentityCard> getCard(Long id, SecretKey masterKey) throws Exception {
        Optional<IdentityCard> cardOpt = cardsRepository. findById(id);

        if (cardOpt.isPresent()) {
            IdentityCard card = cardOpt. get();
            decryptCard(card, masterKey);
            return Optional.of(card);
        }

        return Optional. empty();
    }

    private void decryptCard(IdentityCard card, SecretKey masterKey) throws Exception {
        if (card.getEncryptedData() != null && card.getEncryptionIV() != null) {
            byte[] combined = new byte[card.getEncryptionIV().length + card.getEncryptedData().length];
            System.arraycopy(card.getEncryptionIV(), 0, combined, 0, card.getEncryptionIV().length);
            System. arraycopy(card.getEncryptedData(), 0, combined, card.getEncryptionIV().length, card.getEncryptedData().length);

            String encryptedData = Base64.getEncoder().encodeToString(combined);
            String decryptedJson = aesCipher.decrypt(encryptedData, masterKey);

            Type type = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> cardData = gson.fromJson(decryptedJson, type);
            card.setCardData(cardData);
        }
    }

    public List<IdentityCard> getAllCards() throws Exception {
        return cardsRepository.findAll();
    }

    public List<IdentityCard> getCardsByType(IdentityCard.CardType type) throws Exception {
        return cardsRepository.findByType(type);
    }

    public List<IdentityCard> getExpiringCards(int daysThreshold) throws Exception {
        LocalDate thresholdDate = LocalDate.now().plusDays(daysThreshold);
        return cardsRepository.findExpiringBefore(thresholdDate);
    }

    public List<IdentityCard> getExpiredCards() throws Exception {
        return cardsRepository.findExpired();
    }

    public List<IdentityCard> searchCards(String query) throws Exception {
        return cardsRepository.search(query);
    }

    public void deleteCard(Long id) throws Exception {
        cardsRepository.delete(id);
    }

    public int checkExpiringCardsCount(int daysThreshold) throws Exception {
        return getExpiringCards(daysThreshold).size();
    }

    public CardStatistics getStatistics() throws Exception {
        List<IdentityCard> allCards = getAllCards();

        int total = allCards.size();
        int expired = 0;
        int expiringSoon = 0;
        Map<IdentityCard.CardType, Integer> byType = new HashMap<>();

        for (IdentityCard card : allCards) {
            if (card.isExpired()) {
                expired++;
            } else if (card. getExpiryDate() != null && card.getDaysUntilExpiry() <= 30) {
                expiringSoon++;
            }

            byType.put(card.getCardType(), byType.getOrDefault(card. getCardType(), 0) + 1);
        }

        return new CardStatistics(total, expired, expiringSoon, byType);
    }

    public static class CardStatistics {
        public final int total;
        public final int expired;
        public final int expiringSoon;
        public final Map<IdentityCard.CardType, Integer> byType;

        public CardStatistics(int total, int expired, int expiringSoon, Map<IdentityCard.CardType, Integer> byType) {
            this. total = total;
            this. expired = expired;
            this. expiringSoon = expiringSoon;
            this.byType = byType;
        }
    }
}