package com.passman.desktop.ui.identity;

import com.passman.core.model. IdentityCard;
import com.passman.core.services. IdentityCardsService;
import javafx.beans.property. ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property. SimpleStringProperty;
import javafx. beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx. collections.ObservableList;

/**
 * ViewModel for Identity Cards
 */
public class IdentityCardsViewModel {

    private final IdentityCardsService cardsService;
    private final ObservableList<IdentityCard> cards = FXCollections.observableArrayList();
    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final ObjectProperty<IdentityCard> selectedCard = new SimpleObjectProperty<>();

    public IdentityCardsViewModel(IdentityCardsService cardsService) {
        this.cardsService = cardsService;
    }

    public void loadCards() {
        try {
            cards.clear();
            cards. addAll(cardsService.getAllCards());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ObservableList<IdentityCard> getCards() {
        return cards;
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }

    public ObjectProperty<IdentityCard> selectedCardProperty() {
        return selectedCard;
    }
}