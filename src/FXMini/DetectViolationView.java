package FXMini;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class DetectViolationView {

    private VBox view;
    private Stage ownerStage;
    private index mainApp;
    private ViolationDataService dataService;

    private File selectedImageFile;
    private ImageView previewImage = new ImageView();
    private Label selectedFileName = new Label("No file selected.");
    private Button detectButton;
    private ProgressIndicator progressIndicator = new ProgressIndicator();

    private TextField licensePlateField = new TextField();
    private TextField locationField = new TextField();
    private ComboBox<String> violationTypeCombo = new ComboBox<>();
    private ComboBox<String> vehicleTypeCombo = new ComboBox<>();
    private Button confirmButton;

    public DetectViolationView(Stage ownerStage, index mainApp, ViolationDataService dataService) {
        this.ownerStage = ownerStage;
        this.mainApp = mainApp;
        this.dataService = dataService;
        view = new VBox(30);
        view.setAlignment(Pos.TOP_CENTER);
        view.setPadding(new Insets(40));
        view.getStyleClass().add("detect-violation-view");
        Label title = new Label("Detect New Violation");
        title.getStyleClass().add("view-title");
        Label subtitle = new Label("Upload an image to automatically detect the license plate.");
        subtitle.getStyleClass().add("view-subtitle");
        view.getChildren().addAll(title, subtitle, createStep1Box());
    }

    private Node createStep1Box() {
        VBox step1Container = new VBox(20);
        step1Container.setAlignment(Pos.CENTER);
        step1Container.getStyleClass().add("step-box");
        Label step1Title = new Label("1. Upload Image");
        step1Title.getStyleClass().add("step-title");
        step1Title.setAlignment(Pos.CENTER_LEFT);
        step1Title.setMaxWidth(Double.MAX_VALUE);
        VBox dropZone = new VBox(10);
        dropZone.setAlignment(Pos.CENTER);
        dropZone.getStyleClass().add("drop-zone");
        Label icon = new Label("☁️");
        icon.getStyleClass().add("drop-zone-icon");
        Label dropLabel = new Label("Drag & drop an image here");
        Button browseButton = new Button("Browse file");
        browseButton.getStyleClass().add("secondary-button");
        browseButton.setOnAction(e -> onBrowseFile());
        dropZone.getChildren().addAll(icon, dropLabel, new Label("or"), browseButton);
        detectButton = new Button("Detect Plate");
        detectButton.getStyleClass().add("primary-button");
        detectButton.setDisable(true);
        detectButton.setOnAction(e -> onDetectPlate());
        progressIndicator.setVisible(false);
        step1Container.getChildren().addAll(step1Title, dropZone, selectedFileName, detectButton, progressIndicator);
        return step1Container;
    }

    private void onBrowseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select License Plate Image");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(ownerStage);
        if (file != null) {
            selectedImageFile = file;
            selectedFileName.setText("Selected file: " + selectedImageFile.getName());
            detectButton.setDisable(false);
        }
    }
    
    private void onDetectPlate() {
        if (selectedImageFile == null) return;

        progressIndicator.setVisible(true);
        detectButton.setDisable(true);

        Task<String> detectionTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                String pythonExecutablePath = "Extract_AI/venv/bin/python3";
                String scriptPath = "Extract_AI/extractor.py";

                ProcessBuilder pb = new ProcessBuilder(
                        pythonExecutablePath,
                        scriptPath,
                        selectedImageFile.getAbsolutePath()
                );
                pb.redirectErrorStream(true);

                Process process = pb.start();
                
                String fullOutput = new BufferedReader(new InputStreamReader(process.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));

                process.waitFor();
                return fullOutput;
            }
        };

        detectionTask.setOnSucceeded(e -> {
            progressIndicator.setVisible(false);
            String result = detectionTask.getValue().trim();
            
            if (result.startsWith("ERROR:") || result.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Detection Failed", "Could not extract text. Python script returned: " + result);
                detectButton.setDisable(false);
            } else {
                licensePlateField.setText(result);
                showStep2();
            }
        });

        detectionTask.setOnFailed(e -> {
            progressIndicator.setVisible(false);
            detectButton.setDisable(false);
            showAlert(Alert.AlertType.ERROR, "Execution Error", "Failed to run the Python script. Is the venv path correct?");
            detectionTask.getException().printStackTrace();
        });

        new Thread(detectionTask).start();
    }

    private void showStep2() {
        view.getChildren().clear();
        Label title = new Label("Confirm Violation");
        title.getStyleClass().add("view-title");
        HBox container = new HBox(40);
        container.setAlignment(Pos.CENTER);
        VBox leftPane = createLeftPaneStep2();
        VBox rightPane = createRightPaneStep2();
        container.getChildren().addAll(leftPane, rightPane);
        view.getChildren().addAll(title, container);
    }

    private VBox createLeftPaneStep2() {
        VBox leftPane = new VBox(20);
        leftPane.setAlignment(Pos.CENTER);
        leftPane.getStyleClass().add("step-box");
        Label stepTitle = new Label("1. Uploaded Image");
        stepTitle.getStyleClass().add("step-title");
        try {
            Image image = new Image(selectedImageFile.toURI().toString());
            previewImage.setImage(image);
            previewImage.setFitWidth(300);
            previewImage.setPreserveRatio(true);
            previewImage.getStyleClass().add("preview-image");
        } catch(Exception e) {
            System.err.println("Could not load preview image.");
        }
        leftPane.getChildren().addAll(stepTitle, previewImage);
        return leftPane;
    }

    private VBox createRightPaneStep2() {
        VBox rightPane = new VBox(15);
        rightPane.getStyleClass().add("step-box");
        Label stepTitle = new Label("2. Confirm Violation");
        stepTitle.getStyleClass().add("step-title");
        Label plateLabel = new Label("License Plate");
        licensePlateField.setPromptText("e.g., MH12AB1234");
        Label locationLabel = new Label("Location");
        locationField.setPromptText("e.g., Main St & 1st Ave");
        Label violationTypeLabel = new Label("Violation Type");
        violationTypeCombo.getItems().addAll("Speeding", "Red Light", "Illegal Parking");
        violationTypeCombo.setValue("Speeding");
        Label vehicleTypeLabel = new Label("Vehicle Type");
        vehicleTypeCombo.getItems().addAll("Car", "Bike", "Truck", "Bus");
        vehicleTypeCombo.setValue("Car");
        confirmButton = new Button("✔ Confirm Violation");
        confirmButton.getStyleClass().add("confirm-button");
        confirmButton.setOnAction(e -> onConfirmViolation());
        rightPane.getChildren().addAll(
            stepTitle, plateLabel, licensePlateField,
            locationLabel, locationField, violationTypeLabel, violationTypeCombo,
            vehicleTypeLabel, vehicleTypeCombo, confirmButton
        );
        return rightPane;
    }

    private void onConfirmViolation() {
        String timestamp = new SimpleDateFormat("MMM dd, yyyy h:mm a").format(new Date());
        Violation newViolation = new Violation(
            licensePlateField.getText(),
            timestamp,
            locationField.getText(),
            violationTypeCombo.getValue(),
            "Unpaid",
            vehicleTypeCombo.getValue()
        );
        dataService.addViolation(newViolation);
        showAlert(Alert.AlertType.INFORMATION, "Success", "Violation has been successfully recorded.");
        mainApp.showAllViolations();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(type);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    public Node getView() {
        return view;
    }
}
