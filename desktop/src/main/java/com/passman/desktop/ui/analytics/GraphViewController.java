package com.passman.desktop.ui.analytics;

import com.passman. core.db.DatabaseManager;
import com. passman.core.services.AnalyticsService;
import com.passman.desktop.DialogUtils;
import com.passman. desktop.MainApp;
import com.passman.desktop.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene. control. Label;
import javafx.scene. control.ListView;
import javafx.scene. layout.VBox;

import java.util. Map;

/**
 * Controller for Analytics/Graph View
 */
public class GraphViewController {

    @FXML private Label securityScoreLabel;
    @FXML private Label totalPasswordsLabel;
    @FXML private Label averageStrengthLabel;
    @FXML private Label averageAgeLabel;
    @FXML private Label reusedCountLabel;

    @FXML private PieChart strengthPieChart;
    @FXML private BarChart<String, Number> ageBarChart;
    @FXML private CategoryAxis ageXAxis;
    @FXML private NumberAxis ageYAxis;

    @FXML private ListView<String> recommendationsListView;

    private AnalyticsService analyticsService;

    @FXML
    public void initialize() {
        analyticsService = new AnalyticsService(DatabaseManager.getInstance());

        loadAnalytics();
    }

    private void loadAnalytics() {
        try {
            var masterKey = SessionManager.getInstance().getMasterKey();

            // Load statistics
            var stats = analyticsService.getStatistics(masterKey);

            securityScoreLabel.setText(stats.securityScore + "/100");
            securityScoreLabel.setStyle(getScoreColor(stats.securityScore));

            totalPasswordsLabel. setText(String.valueOf(stats.totalPasswords));
            averageStrengthLabel.setText(stats.averagePasswordStrength + "/100");
            averageAgeLabel.setText(stats.averagePasswordAge + " days");
            reusedCountLabel.setText(String.valueOf(stats.reusedPasswordCount));

            // Load strength distribution
            loadStrengthChart(masterKey);

            // Load age distribution
            loadAgeChart();

            // Load recommendations
            loadRecommendations(masterKey);

        } catch (Exception e) {
            DialogUtils.showError("Error", "Failed to load analytics", e.getMessage());
        }
    }

    private void loadStrengthChart(javax.crypto.SecretKey masterKey) throws Exception {
        Map<String, Integer> distribution = analyticsService.getStrengthDistribution(masterKey);

        javafx.collections.ObservableList<PieChart.Data> pieChartData =
                javafx. collections.FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart. Data(
                        entry.getKey() + " (" + entry.getValue() + ")",
                        entry.getValue()
                ));
            }
        }

        strengthPieChart.setData(pieChartData);
        strengthPieChart.setTitle("Password Strength Distribution");

        // Apply colors
        strengthPieChart.getData().forEach(data -> {
            if (data.getName().startsWith("Strong")) {
                data.getNode().setStyle("-fx-pie-color: #2ECC71;");
            } else if (data.getName().startsWith("Medium")) {
                data. getNode().setStyle("-fx-pie-color: #F39C12;");
            } else {
                data.getNode().setStyle("-fx-pie-color:  #E74C3C;");
            }
        });
    }

    private void loadAgeChart() throws Exception {
        Map<String, Integer> distribution = analyticsService.getAgeDistribution();

        XYChart.Series<String, Number> series = new XYChart. Series<>();
        series.setName("Passwords");

        for (Map. Entry<String, Integer> entry :  distribution.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        ageBarChart.getData().clear();
        ageBarChart. getData().add(series);
        ageBarChart.setTitle("Password Age Distribution");
        ageBarChart.setLegendVisible(false);
    }

    private void loadRecommendations(javax.crypto.SecretKey masterKey) throws Exception {
        var recommendations = analyticsService.getRecommendations(masterKey);

        javafx.collections.ObservableList<String> items =
                javafx.collections.FXCollections.observableArrayList();

        for (var rec : recommendations) {
            items. add(rec.severity + " " + rec.title + "\n   " + rec.description);
        }

        recommendationsListView.setItems(items);
    }

    @FXML
    private void handleRefresh() {
        loadAnalytics();
    }

    @FXML
    private void handleExportReport() {
        DialogUtils.showInfo("Export Report", "Coming Soon",
                "PDF report export will be available in a future update.");
    }

    @FXML
    private void handleBackToDashboard() {
        MainApp.getSceneManager().switchScene("Dashboard");
    }

    private String getScoreColor(int score) {
        if (score >= 80) return "-fx-text-fill: #2ECC71; -fx-font-size: 32px; -fx-font-weight: bold;";
        if (score >= 60) return "-fx-text-fill: #F39C12; -fx-font-size: 32px; -fx-font-weight: bold;";
        return "-fx-text-fill: #E74C3C; -fx-font-size: 32px; -fx-font-weight:  bold;";
    }
}