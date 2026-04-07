package FXMini;

import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Text;

public class PublicSearchView {

    private VBox view;
    private ViolationDataService dataService;
    private VBox resultsContainer;
    private TextField searchField;

    public PublicSearchView(Runnable onAdminPortalClick, ViolationDataService dataService) {
        this.dataService = dataService;

        view = new VBox(20);
        view.setPadding(new Insets(40));
        view.setAlignment(Pos.TOP_CENTER);
        view.getStyleClass().add("public-view");
        
        Label logo = new Label("TrafficEnforce");
        logo.getStyleClass().add("public-view-logo");
        
        Region headerSpacer = new Region();
        HBox.setHgrow(headerSpacer, Priority.ALWAYS);
        
        Button adminPortalButton = new Button("Admin Portal");
        adminPortalButton.getStyleClass().add("outline-button");
        adminPortalButton.setOnAction(e -> onAdminPortalClick.run());
        
        HBox header = new HBox(logo, headerSpacer, adminPortalButton);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(header, new Insets(0, 0, 30, 0));

        Label title = new Label("Traffic Violation Check");
        title.getStyleClass().add("public-view-title");

        Label subtitle = new Label("Enter a vehicle's license plate number to check for any outstanding penalties.");
        subtitle.getStyleClass().add("public-view-subtitle");
        VBox.setMargin(subtitle, new Insets(0, 0, 20, 0));

        VBox searchCard = new VBox(20);
        searchCard.getStyleClass().add("search-card");
        searchCard.setPadding(new Insets(30, 40, 40, 40));
        searchCard.setMaxWidth(600);
        searchCard.setAlignment(Pos.TOP_LEFT);

        Label searchTitle = new Label("Search for Violations");
        searchTitle.getStyleClass().add("search-card-title");

        searchField = new TextField();
        searchField.setPromptText("e.g., MH12AB1234");
        
        Button searchButton = new Button("Search");
        searchButton.getStyleClass().add("primary-button");
        
        HBox searchInputBox = new HBox(10, searchField, searchButton);
        searchInputBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(searchField, Priority.ALWAYS);
        
        searchCard.getChildren().addAll(searchTitle, searchInputBox);
        
        resultsContainer = new VBox(20);
        resultsContainer.setAlignment(Pos.CENTER);

        searchButton.setOnAction(e -> performSearch(searchField.getText()));
        searchField.setOnAction(e -> performSearch(searchField.getText()));

        view.getChildren().addAll(header, title, subtitle, searchCard, resultsContainer);
    }

    private void performSearch(String plateNumber) {
        resultsContainer.getChildren().clear();
        if (plateNumber == null || plateNumber.trim().isEmpty()) {
            return;
        }

        Text resultTitle = new Text("Search Results for \"" + plateNumber.toUpperCase() + "\"");
        resultTitle.getStyleClass().add("result-title");
        resultsContainer.getChildren().add(resultTitle);
        
        ObservableList<Violation> searchResults = dataService.searchByPlate(plateNumber);

        ObservableList<Violation> unpaidViolations = searchResults.stream()
                .filter(v -> "Unpaid".equalsIgnoreCase(v.getFineStatus()))
                .collect(Collectors.toCollection(FXCollections::observableArrayList));

        if (unpaidViolations.isEmpty()) {
            resultsContainer.getChildren().add(createNoViolationsNode());
        } else {
            FlowPane violationCardsPane = new FlowPane(20, 20);
            violationCardsPane.setAlignment(Pos.CENTER);
            for (Violation violation : unpaidViolations) {
                violationCardsPane.getChildren().add(createViolationCard(violation));
            }
            resultsContainer.getChildren().add(violationCardsPane);
        }
    }

    private Node createNoViolationsNode() {
        VBox container = new VBox(15);
        container.getStyleClass().add("no-violations-card");
        container.setAlignment(Pos.CENTER);

        SVGPath icon = new SVGPath();
        icon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1.07 13.29l-2.12-2.12 1.41-1.41 2.12 2.12-4.24-4.24 1.41-1.41 4.24 4.24 2.12-2.12 1.41 1.41-2.12 2.12-1.41-1.41-1.41 1.41z");
        icon.getStyleClass().add("success-icon");

        Label title = new Label("No Violations Found");
        title.getStyleClass().add("no-violations-title");

        Label subtitle = new Label("This license plate has a clean record in our system.");
        subtitle.getStyleClass().add("no-violations-subtitle");
        
        container.getChildren().addAll(icon, title, subtitle);
        return container;
    }

    private Node createViolationCard(Violation violation) {
        VBox card = new VBox(15);
        card.getStyleClass().add("result-card");

        String imageUrl = "https://placehold.co/400x150/facc15/1e293b?text=" + violation.getPlate().replace(" ", "+");
        ImageView imageView = new ImageView();
        try {
            imageView.setImage(new Image(imageUrl, true));
        } catch(Exception e) {
            System.err.println("Failed to load image for result card: " + e.getMessage());
        }
        imageView.setFitWidth(300);
        imageView.setPreserveRatio(true);
        VBox.setMargin(imageView, new Insets(0, 0, 10, 0));

        HBox typeStatusBox = new HBox(10);
        typeStatusBox.setAlignment(Pos.CENTER_LEFT);
        Label violationType = new Label(violation.getViolationType());
        violationType.getStyleClass().add("result-card-type");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(violation.getFineStatus());
        status.getStyleClass().add("status-unpaid-badge");

        typeStatusBox.getChildren().addAll(violationType, spacer, status);

        card.getChildren().addAll(
            imageView,
            typeStatusBox,
            createDetailIconRow("M11.99 2C6.47 2 2 6.48 2 12s4.47 10 9.99 10C17.52 22 22 17.52 22 12S17.52 2 11.99 2zM12 20c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8zm.5-13H11v6l5.25 3.15.75-1.23-4.5-2.67z", violation.getTimestamp()),
            createDetailIconRow("M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z", violation.getLocation())
        );
        return card;
    }

    private Node createDetailIconRow(String svg, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        SVGPath icon = new SVGPath();
        icon.setContent(svg);
        icon.getStyleClass().add("detail-icon");
        
        Label label = new Label(text);
        label.getStyleClass().add("detail-text");
        
        row.getChildren().addAll(icon, label);
        return row;
    }

    public Node getView() {
        return view;
    }
}
