package com.passman.desktop.ui.dashboard;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;

public class PasswordTableViewController {

    @FXML
    private TableView<DashboardViewModel.CredentialItem> credentialsTable;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> titleColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> usernameColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> urlColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> ageBadgeColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> strengthColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, Boolean> reuseColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, Void> actionsColumn;

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        urlColumn.setCellValueFactory(new PropertyValueFactory<>("url"));
        ageBadgeColumn.setCellValueFactory(new PropertyValueFactory<>("ageBadge"));
        strengthColumn.setCellValueFactory(new PropertyValueFactory<>("strength"));
        reuseColumn.setCellValueFactory(new PropertyValueFactory<>("hasReuse"));

        ageBadgeColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Fresh")) {
                        setStyle("-fx-background-color: #d4edda; -fx-text-fill: #155724;");
                    } else if (item.equals("Old")) {
                        setStyle("-fx-background-color: #fff3cd; -fx-text-fill: #856404;");
                    } else {
                        setStyle("-fx-background-color: #f8d7da; -fx-text-fill: #721c24;");
                    }
                }
            }
        });

        strengthColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Strong")) {
                        setTextFill(Color.GREEN);
                    } else if (item.equals("Medium")) {
                        setTextFill(Color.ORANGE);
                    } else {
                        setTextFill(Color.RED);
                    }
                }
            }
        });

        reuseColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "⚠️" : "✓");
                }
            }
        });

        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editBtn = new Button("Edit");
            private final Button deleteBtn = new Button("Delete");

            {
                editBtn.setOnAction(evt -> {
                    DashboardViewModel.CredentialItem item = getTableView().getItems().get(getIndex());
                    handleEdit(item);
                });

                deleteBtn.setOnAction(evt -> {
                    DashboardViewModel.CredentialItem item = getTableView().getItems().get(getIndex());
                    handleDelete(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(new javafx.scene.layout.HBox(5, editBtn, deleteBtn));
                }
            }
        });
    }

    private void handleEdit(DashboardViewModel.CredentialItem item) {
        System.out.println("Edit: " + item.getTitle());
    }

    private void handleDelete(DashboardViewModel. CredentialItem item) {
        System.out.println("Delete: " + item.getTitle());
    }

    public void setItems(javafx.collections.ObservableList<DashboardViewModel.CredentialItem> items) {
        credentialsTable.setItems(items);
    }
}