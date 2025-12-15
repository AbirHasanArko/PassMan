package com.passman.desktop.ui.dashboard;

import javafx.fxml.FXML;
import javafx.scene. control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene. control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;

/**
 * Controller for password table view in dashboard
 */
public class PasswordTableViewController {

    @FXML
    private TableView<DashboardViewModel.CredentialItem> passwordTable;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> titleColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> usernameColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> urlColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> ageColumn;

    @FXML
    private TableColumn<DashboardViewModel.CredentialItem, String> strengthColumn;

    @FXML
    public void initialize() {
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));
        urlColumn. setCellValueFactory(new PropertyValueFactory<>("url"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("ageBadge"));
        strengthColumn. setCellValueFactory(new PropertyValueFactory<>("strength"));
    }

    public void setItems(ObservableList<DashboardViewModel.CredentialItem> items) {
        passwordTable. setItems(items);
    }
}