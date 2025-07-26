package crud;

import crud.AdminCRUD;
import dashboard.AdminDashboard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminLogin {

    public void showLogin(Stage stage) {
        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label heading = new Label("Admin Login");
        heading.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Login");

        Label statusLabel = new Label();

        loginBtn.setOnAction(e -> {
            String username = usernameField.getText();
            String password = passwordField.getText();

            if (AdminCRUD.validateLogin(username, password)) {
                new AdminDashboard().showDashboard(stage);
            } else {
                statusLabel.setText("Invalid credentials");
            }
        });

        root.getChildren().addAll(heading, usernameField, passwordField, loginBtn, statusLabel);
        Scene scene = new Scene(root, 300, 250);
        stage.setTitle("Admin Login");
        stage.setScene(scene);
        stage.show();
    }
}
