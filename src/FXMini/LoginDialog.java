package FXMini;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import java.util.Optional;

public class LoginDialog extends Stage {

    private Optional<Pair<String, String>> result = Optional.empty();

    public LoginDialog(Stage owner) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.TRANSPARENT);

        VBox dialogRoot = new VBox(20);
        dialogRoot.getStyleClass().add("login-dialog");
        dialogRoot.setPadding(new Insets(30));
        dialogRoot.setAlignment(Pos.CENTER);

        Text title = new Text("Admin Login");
        title.setFont(Font.font("System", FontWeight.BOLD, 20));
        title.getStyleClass().add("dialog-title");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter username");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("•••••");

        grid.add(usernameLabel, 0, 0);
        grid.add(usernameField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);

        Button loginButton = new Button("Login");
        loginButton.getStyleClass().add("primary-button");
        loginButton.setDefaultButton(true);

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().add("outline-button");
        
        HBox buttonBar = new HBox(10, cancelButton, loginButton);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        loginButton.setOnAction(e -> {
            result = Optional.of(new Pair<>(usernameField.getText(), passwordField.getText()));
            close();
        });
        
        cancelButton.setOnAction(e -> close());

        dialogRoot.getChildren().addAll(title, grid, buttonBar);

        Scene scene = new Scene(dialogRoot);
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        setScene(scene);
    }

    public Optional<Pair<String, String>> showDialogAndWait() {
        super.showAndWait();
        return result;
    }
}
