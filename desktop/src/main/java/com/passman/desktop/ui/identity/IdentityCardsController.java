package com.passman.desktop.ui.identity;

import com.passman.core.db. DatabaseManager;
import com.passman.core.model.IdentityCard;
import com.passman.core.repository.IdentityCardsRepositoryImpl;
import com.passman.core. services.IdentityCardsService;
import com.passman. desktop.DialogUtils;
import com.passman.desktop.MainApp;
import com.passman. desktop.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections. ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx. scene.layout.FlowPane;
import javafx. scene.layout.VBox;

import java.time.LocalDate;
import java. util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for Identity Cards
 */
public class IdentityCardsController {

    @FXML private FlowPane cardsFlowPane;
    @FXML private TextField searchField;
    @FXML private ComboBox<IdentityCard.CardType> cardTypeComboBox;
    @FXML private Label statsLabel;

    private IdentityCardsService cardsService;
    private IdentityCardsViewModel viewModel;

    @FXML
    public void initialize() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
        IdentityCardsRepositoryImpl repository = new IdentityCardsRepositoryImpl(dbManager);
        cardsService = new IdentityCardsService(repository);
        viewModel = new IdentityCardsViewModel(cardsService);

        cardTypeComboBox.setItems(FXCollections.observableArrayList(IdentityCard.CardType.values()));

        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            performSearch(newVal);
        });

        loadAllCards();
        updateStatistics();
    }

    private void loadAllCards() {
        try {
            List<IdentityCard> cards = cardsService.getAllCards();
            displayCards(cards);
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load cards", e.getMessage());
        }
    }

    private void displayCards(List<IdentityCard> cards) {
        cardsFlowPane.getChildren().clear();

        for (IdentityCard card : cards) {
            IdentityCardWidget widget = new IdentityCardWidget(card);
            widget.setOnClick(() -> openCardEditor(card));
            cardsFlowPane.getChildren().add(widget. getNode());
        }
    }

    private void performSearch(String query) {
        if (query == null || query. trim().isEmpty()) {
            loadAllCards();
            return;
        }

        try {
            List<IdentityCard> results = cardsService.searchCards(query);
            displayCards(results);
        } catch (Exception e) {
            DialogUtils.showError("Error", "Search failed", e.getMessage());
        }
    }

    private void updateStatistics() {
        try {
            var stats = cardsService.getStatistics();
            statsLabel.setText(String.format(
                    "Total:  %d | Expired: %d | Expiring Soon: %d",
                    stats. total, stats.expired, stats.expiringSoon
            ));
        } catch (Exception e) {
            statsLabel.setText("Statistics unavailable");
        }
    }

    @FXML
    private void handleAddCard() {
        openCardEditor(null);
    }

    private void openCardEditor(IdentityCard card) {
        Dialog<IdentityCard> dialog = new Dialog<>();
        dialog.setTitle(card == null ? "Add Identity Card" : "Edit Identity Card");
        dialog.setHeaderText("Enter card details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        ComboBox<IdentityCard. CardType> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(IdentityCard. CardType.values()));
        typeCombo.setValue(card != null ? card.getCardType() : IdentityCard.CardType.NATIONAL_ID);

        TextField nameField = new TextField();
        nameField.setPromptText("Card Name");
        if (card != null) nameField.setText(card.getCardName());

        TextField countryField = new TextField();
        countryField.setPromptText("Issuing Country");
        if (card != null) countryField.setText(card.getIssuingCountry());

        DatePicker expiryDatePicker = new DatePicker();
        expiryDatePicker.setPromptText("Expiry Date");
        if (card != null && card.getExpiryDate() != null) {
            expiryDatePicker.setValue(card.getExpiryDate());
        }

        TextField tagsField = new TextField();
        tagsField.setPromptText("Tags (comma separated)");
        if (card != null) tagsField.setText(card. getTags());

        content.getChildren().addAll(
                new Label("Card Type:"), typeCombo,
                new Label("Card Name:"), nameField,
                new Label("Issuing Country: "), countryField,
                new Label("Expiry Date:"), expiryDatePicker,
                new Label("Tags:"), tagsField
        );

        dialog.getDialogPane().setContent(content);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    IdentityCard newCard = card != null ?  card :  new IdentityCard();
                    newCard.setCardType(typeCombo.getValue());
                    newCard.setCardName(nameField.getText());
                    newCard.setIssuingCountry(countryField.getText());
                    newCard.setExpiryDate(expiryDatePicker.getValue());
                    newCard.setTags(tagsField.getText());

                    Map<String, String> cardData = new HashMap<>();
                    cardData.put("cardName", nameField.getText());
                    cardData.put("country", countryField.getText());
                    newCard.setCardData(cardData);

                    cardsService.saveCard(newCard, SessionManager.getInstance().getMasterKey());
                    return newCard;
                } catch (Exception e) {
                    DialogUtils.showError("Error", "Failed to save card", e.getMessage());
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(result -> {
            loadAllCards();
            updateStatistics();
        });
    }

    @FXML
    private void handleFilterByType() {
        IdentityCard.CardType selectedType = cardTypeComboBox. getValue();
        if (selectedType != null) {
            try {
                List<IdentityCard> filtered = cardsService.getCardsByType(selectedType);
                displayCards(filtered);
            } catch (Exception e) {
                DialogUtils.showError("Error", "Failed to filter cards", e.getMessage());
            }
        }
    }

    @FXML
    private void handleShowExpiring() {
        try {
            List<IdentityCard> expiring = cardsService.getExpiringCards(30);
            displayCards(expiring);
            DialogUtils.showInfo("Expiring Cards", "Cards Expiring Soon",
                    expiring.size() + " cards expire within 30 days");
        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load expiring cards", e.getMessage());
        }
    }

    @FXML
    private void handleShowExpired() {
        try {
            List<IdentityCard> expired = cardsService.getExpiredCards();
            displayCards(expired);
            DialogUtils.showInfo("Expired Cards", "Expired Cards",
                    expired. size() + " cards have expired");
        } catch (Exception e) {
            DialogUtils. showError("Error", "Failed to load expired cards", e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }
}