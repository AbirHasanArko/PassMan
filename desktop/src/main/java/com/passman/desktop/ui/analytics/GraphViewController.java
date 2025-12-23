package com.passman.desktop.ui.analytics;

import com.passman.core.db.DatabaseManager;
import com.passman.core.services. AnalyticsService;
import com.passman.desktop.DialogUtils;
import com. passman.desktop.MainApp;
import com.passman.desktop.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.util.Map;

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
        try {
            analyticsService = new AnalyticsService(DatabaseManager.getInstance());

            System.out.println("✅ GraphViewController initialized successfully");

            loadAnalytics();
        } catch (Exception e) {
            System.err.println("❌ Failed to initialize GraphViewController:  " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showError("Initialization Error",
                    "Failed to initialize Analytics", e.getMessage());
        }
    }

    private void loadAnalytics() {
        try {
            var masterKey = SessionManager.getInstance().getMasterKey();

            if (masterKey == null) {
                throw new IllegalStateException("Master key not found in session.  Please log in again.");
            }

            // Load statistics
            var stats = analyticsService.getStatistics(masterKey);

            if (stats == null) {
                throw new IllegalStateException("Failed to retrieve analytics statistics");
            }

            // Update UI labels with null checks
            if (securityScoreLabel != null) {
                securityScoreLabel.setText(stats.securityScore + "/100");
                securityScoreLabel.setStyle(getScoreColor(stats.securityScore));
            }

            if (totalPasswordsLabel != null) {
                totalPasswordsLabel.setText(String.valueOf(stats.totalPasswords));
            }

            if (averageStrengthLabel != null) {
                averageStrengthLabel. setText(stats.averagePasswordStrength + "/100");
            }

            if (averageAgeLabel != null) {
                averageAgeLabel.setText(stats.averagePasswordAge + " days");
            }

            if (reusedCountLabel != null) {
                reusedCountLabel.setText(String.valueOf(stats.reusedPasswordCount));
            }

            // Load charts
            loadStrengthChart(masterKey);
            loadAgeChart();
            loadRecommendations(masterKey);

            System.out.println("✅ Analytics loaded successfully");

        } catch (Exception e) {
            System.err.println("❌ Failed to load analytics:  " + e.getMessage());
            e.printStackTrace();
            DialogUtils.showError("Error", "Failed to load analytics",
                    e.getMessage() + "\n\nPlease ensure you have credentials saved.");
        }
    }

    private void loadStrengthChart(javax.crypto.SecretKey masterKey) throws Exception {
        if (strengthPieChart == null) {
            System.out.println("⚠️ strengthPieChart is null, skipping");
            return;
        }

        try {
            Map<String, Integer> distribution = analyticsService.getStrengthDistribution(masterKey);

            if (distribution == null || distribution.isEmpty()) {
                System.out.println("⚠️ No password strength data available");
                strengthPieChart.setTitle("Password Strength Distribution (No Data)");
                return;
            }

            javafx.collections.ObservableList<PieChart. Data> pieChartData =
                    javafx.collections. FXCollections.observableArrayList();

            for (Map. Entry<String, Integer> entry :  distribution.entrySet()) {
                if (entry.getValue() > 0) {
                    pieChartData.add(new PieChart.Data(
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
                    data.getNode().setStyle("-fx-pie-color: #F39C12;");
                } else {
                    data.getNode().setStyle("-fx-pie-color: #E74C3C;");
                }
            });

            System.out.println("✅ Strength chart loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load strength chart: " + e.getMessage());
            throw e;
        }
    }

    private void loadAgeChart() throws Exception {
        if (ageBarChart == null) {
            System.out.println("⚠️ ageBarChart is null, skipping");
            return;
        }

        try {
            Map<String, Integer> distribution = analyticsService.getAgeDistribution();

            if (distribution == null || distribution.isEmpty()) {
                System.out.println("⚠️ No password age data available");
                ageBarChart.setTitle("Password Age Distribution (No Data)");
                return;
            }

            XYChart.Series<String, Number> series = new XYChart. Series<>();
            series.setName("Passwords");

            for (Map. Entry<String, Integer> entry :  distribution.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }

            ageBarChart.getData().clear();
            ageBarChart. getData().add(series);
            ageBarChart.setTitle("Password Age Distribution");
            ageBarChart.setLegendVisible(false);

            System.out.println("✅ Age chart loaded");
        } catch (Exception e) {
            System.err.println("❌ Failed to load age chart: " + e.getMessage());
            throw e;
        }
    }

    private void loadRecommendations(javax.crypto.SecretKey masterKey) throws Exception {
        if (recommendationsListView == null) {
            System.out.println("⚠️ recommendationsListView is null, skipping");
            return;
        }

        try {
            var recommendations = analyticsService.getRecommendations(masterKey);

            javafx.collections.ObservableList<String> items =
                    javafx.collections. FXCollections.observableArrayList();

            if (recommendations == null || recommendations.isEmpty()) {
                items.add("✅ No recommendations - Great security posture!");
            } else {
                for (var rec : recommendations) {
                    items.add(rec. severity + " " + rec.title + "\n   " + rec.description);
                }
            }

            recommendationsListView.setItems(items);

            System.out.println("✅ Recommendations loaded:  " + items.size() + " items");
        } catch (Exception e) {
            System.err.println("❌ Failed to load recommendations: " + e.getMessage());
            throw e;
        }
    }

    @FXML
    private void handleRefresh() {
        System.out.println("🔄 Refreshing analytics.. .");
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