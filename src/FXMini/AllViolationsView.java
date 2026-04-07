package FXMini;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;

public class AllViolationsView {

    private VBox view;
    private FlowPane violationsGrid;
    private ViolationDataService dataService;

    public AllViolationsView(ViolationDataService dataService) {
        this.dataService = dataService;

        view = new VBox(20);
        view.setPadding(new Insets(30));
        view.getStyleClass().add("admin-view");

        Label title = new Label("All Violations");
        title.getStyleClass().add("admin-view-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Search by license plate...");
        searchField.getStyleClass().add("search-field");
        
        HBox header = new HBox(title, searchField);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        header.setAlignment(Pos.CENTER_LEFT);

        violationsGrid = new FlowPane(20, 20);
        violationsGrid.setAlignment(Pos.TOP_LEFT);

        ScrollPane scrollPane = new ScrollPane(violationsGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("scroll-pane");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        refresh();

        view.getChildren().addAll(header, scrollPane);
    }

    public void refresh() {
        violationsGrid.getChildren().clear();
        for (Violation violation : dataService.getViolations()) {
            violationsGrid.getChildren().add(createViolationCard(violation));
        }
    }

    private Node createViolationCard(Violation violation) {
        VBox card = new VBox(15);
        card.getStyleClass().add("violation-card");

        String imageUrl = "https://placehold.co/400x150/facc15/1e293b?text=" + violation.getPlate().replace(" ", "+");
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            System.err.println("Failed to load placeholder image: " + e.getMessage());
        }
        imageView.setFitHeight(150);
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(false);
        imageView.getStyleClass().add("violation-image");

        VBox details = new VBox(5);
        
        Label plateLabel = new Label(violation.getPlate());
        plateLabel.getStyleClass().add("violation-card-plate");

        HBox subDetails = new HBox(15);
        subDetails.getChildren().addAll(
            createDetailIconRow("M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z", violation.getTimestamp()),
            createDetailIconRow("M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z", violation.getLocation())
        );
        details.getChildren().addAll(plateLabel, subDetails);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(violation.getFineStatus());
        status.getStyleClass().addAll(
            "status-badge",
            violation.getFineStatus().equalsIgnoreCase("Paid") ? "status-paid" : "status-unpaid"
        );

        status.textProperty().bind(violation.fineStatusProperty());

        ComboBox<String> statusToggle = new ComboBox<>();
        statusToggle.getItems().addAll("Paid", "Unpaid");
        statusToggle.setValue(violation.getFineStatus());
        statusToggle.getStyleClass().add("status-toggle");

        statusToggle.setOnAction(e -> {
            String newStatus = statusToggle.getValue();
            dataService.updateViolationStatus(violation.getPlate(), newStatus);
        });

        violation.fineStatusProperty().addListener((obs, oldStatus, newStatus) -> {
            statusToggle.setValue(newStatus);
            status.getStyleClass().removeAll("status-paid", "status-unpaid");
            status.getStyleClass().add(newStatus.equalsIgnoreCase("Paid") ? "status-paid" : "status-unpaid");
        });

        card.getChildren().addAll(imageView, details, spacer, statusToggle);
        return card;
    }

    private HBox createDetailIconRow(String svg, String text) {
        HBox row = new HBox(5);
        row.setAlignment(Pos.CENTER_LEFT);
        
        SVGPath icon = new SVGPath();
        icon.setContent(svg);
        icon.getStyleClass().add("detail-icon-small");
        
        Label label = new Label(text);
        label.getStyleClass().add("detail-text-small");
        
        row.getChildren().addAll(icon, label);
        return row;
    }

    public Node getView() {
        return view;
    }
}
