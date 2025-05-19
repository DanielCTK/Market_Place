package ui;

// Import JFoenix components
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXSpinner;

import Services.DatabaseManager;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*; // Giữ lại các import JavaFX chuẩn khác
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class NewSalesReportController implements Initializable {

    // Sử dụng kiểu JFoenix
    @FXML private JFXListView<String> categoryListView;
    @FXML private JFXButton generateReportButton;
    @FXML private JFXSpinner progressIndicator; // JFXSpinner thay cho ProgressIndicator

    @FXML private Label totalSalesLabel; // Label vẫn là JavaFX chuẩn

    @FXML private TableView<SaleEntry> salesTableView; // TableView vẫn là JavaFX chuẩn
    @FXML private TableColumn<SaleEntry, String> productIdColumn;
    @FXML private TableColumn<SaleEntry, String> productNameColumn;
    @FXML private TableColumn<SaleEntry, Integer> quantitySoldColumn;
    @FXML private TableColumn<SaleEntry, Double> unitPriceColumn;
    @FXML private TableColumn<SaleEntry, Double> totalRevenueColumn;

    // Sử dụng kiểu JFoenix
    @FXML private JFXRadioButton barChartRadio;
    @FXML private JFXRadioButton lineChartRadio;
    @FXML private JFXRadioButton pieChartRadio;

    @FXML private ToggleGroup chartTypeToggleGroup; // ToggleGroup vẫn là JavaFX chuẩn

    // Charts vẫn là JavaFX chuẩn
    @FXML private BarChart<String, Number> salesBarChart;
    @FXML private CategoryAxis barChartXAxis;
    @FXML private NumberAxis barChartYAxis;

    @FXML private LineChart<String, Number> salesLineChart;
    @FXML private CategoryAxis lineChartXAxis;
    @FXML private NumberAxis lineChartYAxis;

    @FXML private PieChart salesPieChart;

    private ObservableList<SaleEntry> salesData = FXCollections.observableArrayList();
    private ObservableList<String> productCategories = FXCollections.observableArrayList();
    private ExecutorService executorService;

    private List<SaleEntry> lastFetchedSalesEntries = new ArrayList<>();
    private Map<String, Double> lastAggregatedCategoryData = new HashMap<>();
    private boolean hasFetchedDataSinceLastFilterChange = false;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });

        // Các thiết lập cho TableColumn vẫn giữ nguyên
        if (productIdColumn != null) productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productId"));
        else System.err.println("NewSalesReportController: productIdColumn is null.");
        // ... (các TableColumn khác tương tự) ...
        if (productNameColumn != null) productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        else System.err.println("NewSalesReportController: productNameColumn is null.");
        if (quantitySoldColumn != null) quantitySoldColumn.setCellValueFactory(new PropertyValueFactory<>("quantitySold"));
        else System.err.println("NewSalesReportController: quantitySoldColumn is null.");
        if (unitPriceColumn != null) unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        else System.err.println("NewSalesReportController: unitPriceColumn is null.");
        if (totalRevenueColumn != null) totalRevenueColumn.setCellValueFactory(new PropertyValueFactory<>("totalRevenue"));
        else System.err.println("NewSalesReportController: totalRevenueColumn is null.");


        if (salesTableView != null) {
            salesTableView.setItems(salesData);
            salesTableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        } else {
            System.err.println("NewSalesReportController: salesTableView is null.");
        }

        if (categoryListView != null) { // JFXListView
            categoryListView.setItems(productCategories);
            categoryListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            System.out.println("DEBUG NewSalesReportController: categoryListView SelectionMode set to: " + categoryListView.getSelectionModel().getSelectionMode());
            // JFXListView dùng selectedItemProperty hoặc itemsProperty tùy theo cách bạn muốn theo dõi
            // Để đơn giản, chúng ta có thể không cần listener ở đây nếu việc fetch lại dữ liệu được trigger bởi nút "Generate Report"
            // Hoặc, nếu bạn muốn nó tự động đánh dấu khi lựa chọn thay đổi:
            categoryListView.getSelectionModel().selectedItemProperty().addListener((obs, ov, nv) -> {
                 hasFetchedDataSinceLastFilterChange = false;
            });
        } else System.err.println("NewSalesReportController: categoryListView (JFXListView) is null.");


        loadProductCategories();

        if (salesBarChart != null) salesBarChart.setAnimated(false); else System.err.println("NewSalesReportController: salesBarChart is null.");
        if (salesLineChart != null) salesLineChart.setAnimated(false); else System.err.println("NewSalesReportController: salesLineChart is null.");
        if (salesPieChart != null) salesPieChart.setAnimated(false); else System.err.println("NewSalesReportController: salesPieChart is null.");

        if (chartTypeToggleGroup != null && chartTypeToggleGroup.selectedToggleProperty() != null) {
             chartTypeToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
                if (newToggle != null) {
                    updateChartVisibility();
                    if (hasFetchedDataSinceLastFilterChange && !lastFetchedSalesEntries.isEmpty()) {
                        populateChartsWithLastData();
                    }
                }
            });
        } else System.err.println("NewSalesReportController: chartTypeToggleGroup or its selectedToggleProperty is null.");
       
        updateChartVisibility();
    }

    // ... (các phương thức còn lại loadProductCategories, updateChartVisibility, handleGenerateReport, populateChartsWithLastData, shutdownExecutor, showAlert, ReportResult, SaleEntry giữ nguyên như trước)
    // Đảm bảo các phương thức này không có lỗi và logic vẫn đúng. Mình sẽ không lặp lại chúng ở đây cho ngắn gọn.
    // Chỉ cần chắc chắn rằng bạn đã copy đúng các phương thức đó từ phiên bản trước.
    // Ví dụ, phương thức showAlert vẫn cần thiết.
    private void loadProductCategories() {
        Task<List<String>> loadCategoriesTask = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                List<String> categories = new ArrayList<>();
                String query = "SELECT DISTINCT product_category_name FROM products WHERE product_category_name IS NOT NULL AND product_category_name <> '' ORDER BY product_category_name";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query);
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        categories.add(rs.getString("product_category_name"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Failed to load product categories", e);
                }
                return categories;
            }
        };
        loadCategoriesTask.setOnSucceeded(event -> {
            if (productCategories != null) {
                 productCategories.setAll(loadCategoriesTask.getValue());
            }
        });
        loadCategoriesTask.setOnFailed(event -> {
            Throwable ex = loadCategoriesTask.getException();
            if (ex != null) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load product categories: " + ex.getMessage());
            } else {
                showAlert(Alert.AlertType.ERROR, "Load Error", "Could not load product categories: Unknown error.");
            }
        });
        if (executorService != null) {
            executorService.submit(loadCategoriesTask);
        } else {
            System.err.println("NewSalesReportController: executorService is null, cannot load categories.");
        }
    }

    private void updateChartVisibility() {
        if (salesBarChart == null || barChartRadio == null || salesLineChart == null || lineChartRadio == null || salesPieChart == null || pieChartRadio == null) {
            System.err.println("NewSalesReportController: One or more chart/radio components are null in updateChartVisibility.");
            return;
        }
        salesBarChart.setVisible(barChartRadio.isSelected());
        salesBarChart.setManaged(barChartRadio.isSelected());
        salesLineChart.setVisible(lineChartRadio.isSelected());
        salesLineChart.setManaged(lineChartRadio.isSelected());
        salesPieChart.setVisible(pieChartRadio.isSelected());
        salesPieChart.setManaged(pieChartRadio.isSelected());
    }

    @FXML
    void handleGenerateReport(ActionEvent event) {
        if (categoryListView == null || categoryListView.getSelectionModel() == null) {
             showAlert(Alert.AlertType.WARNING, "Input Missing", "Category list view is not available.");
            return;
        }
        ObservableList<String> selectedCategories = categoryListView.getSelectionModel().getSelectedItems();

        if (selectedCategories.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Selection Required", "Please select at least one category to generate the report.");
            return;
        }

        if (progressIndicator != null) progressIndicator.setVisible(true);
        if (generateReportButton != null) generateReportButton.setDisable(true);
        if (salesData != null) salesData.clear();

        Task<ReportResult> reportTask = new Task<>() {
            @Override
            protected ReportResult call() throws Exception {
                List<SaleEntry> currentFetchedSales = new ArrayList<>();
                Map<String, Double> currentAggregatedData = new HashMap<>();
                double currentOverallTotal = 0;

                StringBuilder sqlBuilder = new StringBuilder(
                    "SELECT p.product_id, p.product_category_name, " +
                    "COUNT(oi.order_items_pk) AS quantity_sold, " +
                    "AVG(oi.price) AS average_unit_price, " +
                    "SUM(oi.price) AS total_revenue_for_product " +
                    "FROM order_items oi " +
                    "JOIN orders o ON oi.order_id = o.order_id " +
                    "JOIN products p ON oi.product_id = p.product_id "
                );
                List<Object> params = new ArrayList<>();
                if (!selectedCategories.isEmpty()) {
                    String placeholders = String.join(",", java.util.Collections.nCopies(selectedCategories.size(), "?"));
                    sqlBuilder.append("WHERE p.product_category_name IN (").append(placeholders).append(") ");
                    params.addAll(selectedCategories);
                }

                sqlBuilder.append("GROUP BY p.product_id, p.product_category_name ");
                sqlBuilder.append("ORDER BY p.product_category_name, total_revenue_for_product DESC");

                String finalQuery = sqlBuilder.toString();
                System.out.println("Executing Query: " + finalQuery + " with params: " + selectedCategories);

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(finalQuery)) {
                    for (int i = 0; i < params.size(); i++) {
                        pstmt.setString(i + 1, (String) params.get(i));
                    }
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String prodId = rs.getString("product_id");
                            String prodCategoryName = rs.getString("product_category_name");
                            int qty = rs.getInt("quantity_sold");
                            double avgUnitPrice = rs.getDouble("average_unit_price");
                            double totalRevenueForProduct = rs.getDouble("total_revenue_for_product");

                            currentFetchedSales.add(new SaleEntry(prodId, prodCategoryName, qty, avgUnitPrice, totalRevenueForProduct));
                            currentOverallTotal += totalRevenueForProduct;
                            currentAggregatedData.put(prodCategoryName, currentAggregatedData.getOrDefault(prodCategoryName, 0.0) + totalRevenueForProduct);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Database error fetching sales report: " + e.getMessage(), e);
                }
                return new ReportResult(currentFetchedSales, currentOverallTotal, currentAggregatedData);
            }
        };

        reportTask.setOnSucceeded(e -> {
            ReportResult result = reportTask.getValue();
            if (salesData != null) salesData.setAll(result.saleEntries());
            if (totalSalesLabel != null) totalSalesLabel.setText(String.format("$%.2f", result.overallTotalSales()));

            lastFetchedSalesEntries = new ArrayList<>(result.saleEntries());
            lastAggregatedCategoryData = new HashMap<>(result.chartDataAggregator());
            hasFetchedDataSinceLastFilterChange = true;

            populateChartsWithLastData();
            updateChartVisibility();

            if (progressIndicator != null) progressIndicator.setVisible(false);
            if (generateReportButton != null) generateReportButton.setDisable(false);
        });

        reportTask.setOnFailed(e -> {
            if (progressIndicator != null) progressIndicator.setVisible(false);
            if (generateReportButton != null) generateReportButton.setDisable(false);
            Throwable ex = reportTask.getException();
            if (ex != null) {
                 ex.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Report Generation Failed", "An error occurred: " + ex.getMessage());
            } else {
                showAlert(Alert.AlertType.ERROR, "Report Generation Failed", "An unknown error occurred.");
            }
        });
        if (executorService != null) {
            executorService.submit(reportTask);
        } else {
            System.err.println("NewSalesReportController: executorService is null, cannot generate report.");
        }
    }

    private void populateChartsWithLastData() {
        if (!hasFetchedDataSinceLastFilterChange) return;
        
        if (salesBarChart == null || salesLineChart == null || salesPieChart == null) {
            System.err.println("NewSalesReportController: One or more charts are null in populateChartsWithLastData.");
            return;
        }

        salesBarChart.getData().clear();
        salesLineChart.getData().clear();
        salesPieChart.getData().clear();

        String chartTitle = "Sales";
        if (categoryListView != null && categoryListView.getSelectionModel() != null) {
            ObservableList<String> currentSelectedCategories = categoryListView.getSelectionModel().getSelectedItems();
            if (!currentSelectedCategories.isEmpty()) {
                String categoriesString = currentSelectedCategories.size() > 3 ?
                    String.join(", ", currentSelectedCategories.subList(0, 3)) + ", ..." :
                    String.join(", ", currentSelectedCategories);
                chartTitle += " for " + categoriesString;
            }
        }

        XYChart.Series<String, Number> barSeries = new XYChart.Series<>();
        barSeries.setName("Revenue");
        XYChart.Series<String, Number> lineSeries = new XYChart.Series<>();
        lineSeries.setName("Revenue");

        lastAggregatedCategoryData.forEach((categoryName, sum) -> {
            barSeries.getData().add(new XYChart.Data<>(categoryName, sum));
            lineSeries.getData().add(new XYChart.Data<>(categoryName, sum));
        });

        if (!barSeries.getData().isEmpty()) {
            salesBarChart.getData().add(barSeries);
            salesBarChart.setTitle(chartTitle);
        }
        if (!lineSeries.getData().isEmpty()) {
            salesLineChart.getData().add(lineSeries);
            salesLineChart.setTitle(chartTitle);
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        lastAggregatedCategoryData.forEach((categoryName, sum) -> {
            pieChartData.add(new PieChart.Data(String.format("%s ($%.2f)", categoryName, sum), sum));
        });

        if (!pieChartData.isEmpty()) {
            salesPieChart.setData(pieChartData);
            salesPieChart.setTitle(chartTitle + " Distribution");
        }
    }

    public void shutdownExecutor() {
        System.out.println("NewSalesReportController: Shutting down executor service.");
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Node ownerNode = null;
        if (generateReportButton != null && generateReportButton.getScene() != null) {
            ownerNode = generateReportButton;
        } else if (categoryListView != null && categoryListView.getScene() != null) {
            ownerNode = categoryListView;
        }
        if (ownerNode != null && ownerNode.getScene().getWindow() != null) {
             alert.initOwner(ownerNode.getScene().getWindow());
        }
        alert.showAndWait();
    }

    private record ReportResult(List<SaleEntry> saleEntries, double overallTotalSales, Map<String, Double> chartDataAggregator) {}

    public static class SaleEntry {
        private final SimpleStringProperty productId;
        private final SimpleStringProperty productName;
        private final SimpleIntegerProperty quantitySold;
        private final SimpleDoubleProperty unitPrice;
        private final SimpleDoubleProperty totalRevenue;

        public SaleEntry(String productId, String productName, int quantitySold, double unitPrice, double totalRevenueFromDb) {
            this.productId = new SimpleStringProperty(productId);
            this.productName = new SimpleStringProperty(productName);
            this.quantitySold = new SimpleIntegerProperty(quantitySold);
            this.unitPrice = new SimpleDoubleProperty(unitPrice);
            this.totalRevenue = new SimpleDoubleProperty(totalRevenueFromDb);
        }

        public String getProductId() { return productId.get(); }
        public String getProductName() { return productName.get(); }
        public int getQuantitySold() { return quantitySold.get(); }
        public double getUnitPrice() { return unitPrice.get(); }
        public double getTotalRevenue() { return totalRevenue.get(); }

        public SimpleStringProperty productIdProperty() { return productId; }
        public SimpleStringProperty productNameProperty() { return productName; }
        public SimpleIntegerProperty quantitySoldProperty() { return quantitySold; }
        public SimpleDoubleProperty unitPriceProperty() { return unitPrice; }
        public SimpleDoubleProperty totalRevenueProperty() { return totalRevenue; }
    }

}