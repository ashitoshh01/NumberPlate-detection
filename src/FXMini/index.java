package FXMini;

import java.util.Optional;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.util.Pair;

public class index extends Application {

    private Stage primaryStage;
    private BorderPane root;
    private ViolationDataService dataService = new ViolationDataService();

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.root = new BorderPane();
        
        showPublicSearchView();

        Scene scene = new Scene(root, 1280, 800);
        String cssPath = "/styles.css";
        try {
             scene.getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not find CSS file: " + cssPath);
            showAlert("Styling Error", "The application's CSS file could not be loaded. The UI will not be styled correctly.");
        }

        primaryStage.setTitle("TrafficEnforce");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(768);
        primaryStage.show();
    }

    public void showLoginDialog() {
        LoginDialog loginDialog = new LoginDialog(primaryStage);
        Optional<Pair<String, String>> result = loginDialog.showDialogAndWait();

        result.ifPresent(credentials -> {
            if ("adminJava".equals(credentials.getKey()) && "123".equals(credentials.getValue())) {
                showAdminDashboard();
            } else {
                showAlert("Login Failed", "Invalid username or password.");
            }
        });
    }

    private void showAdminDashboard() {
        Sidebar sidebar = new Sidebar(this);
        root.setLeft(sidebar.getView());
        showAllViolations();
    }

    public void showAllViolations() {
        AllViolationsView allViolationsView = new AllViolationsView(dataService);
        root.setCenter(allViolationsView.getView());
    }

    public void showDetectViolationView() {
        DetectViolationView detectViolationView = new DetectViolationView(primaryStage, this, dataService);
        root.setCenter(detectViolationView.getView());
    }

    public void showPublicSearchView() {
        PublicSearchView publicSearchView = new PublicSearchView(this::showLoginDialog, dataService);
        root.setLeft(null);
        root.setCenter(publicSearchView.getView());
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
