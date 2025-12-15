package com.passman.desktop. ui.identity;

import com. passman.core.model. IdentityCard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * Widget for displaying identity card in grid
 */
public class IdentityCardWidget {

    private final IdentityCard card;
    private final VBox node;
    private Runnable onClickHandler;

    public IdentityCardWidget(IdentityCard card) {
        this.card = card;
        this.node = createNode();
    }

    private VBox createNode() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(15));
        container.setPrefSize(200, 150);
        container. setAlignment(Pos.TOP_LEFT);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");

        // Card type icon
        Label iconLabel = new Label(card.getCardType().getIcon());
        iconLabel.setStyle("-fx-font-size: 32px;");

        // Card name
        Label nameLabel = new Label(card.getCardName());
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Card type
        Label typeLabel = new Label(card.getCardType().getDisplayName());
        typeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");

        // Expiry status
        Label expiryLabel = new Label(card.getExpiryStatus());
        if (card.isExpired()) {
            expiryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #dc3545; -fx-font-weight: bold;");
        } else if ("Expires Soon".equals(card.getExpiryStatus())) {
            expiryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill:  #ffc107; -fx-font-weight: bold;");
        } else {
            expiryLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #28a745;");
        }

        container.getChildren().addAll(iconLabel, nameLabel, typeLabel, expiryLabel);

        // Add click handler
        container.setOnMouseClicked(event -> {
            if (onClickHandler != null) {
                onClickHandler.run();
            }
        });

        // Hover effect
        container.setOnMouseEntered(event -> {
            container.setStyle("-fx-background-color: #f0f0f0; -fx-background-radius: 10; -fx-border-radius:  10; -fx-effect:  dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0, 0, 3); -fx-cursor: hand;");
        });

        container.setOnMouseExited(event -> {
            container.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        });

        return container;
    }

    public VBox getNode() {
        return node;
    }

    public void setOnClick(Runnable handler) {
        this.onClickHandler = handler;
    }
}