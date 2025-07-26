package crud;

import db.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminLogin {

    public static void show(Stage stage) {
        Label title = new Label("🔒 Admin Login");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String username = usernameField.getText().trim();
            String password = passwordField.getText().trim();

            if (username.isEmpty() || password.isEmpty()) {
                errorLabel.setText("Please enter both fields.");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                String query = "SELECT * FROM admins WHERE username = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.checkPassword(password, storedHash)) {
                        // Login success — redirect to admin dashboard
                        new dashboard.AdminDashboard(username).show(stage);
                    } else {
                        errorLabel.setText("Invalid password.");
                    }
                } else {
                    errorLabel.setText("Admin not found.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                errorLabel.setText("⚠ Database error.");
            }
        });

        VBox root = new VBox(10, title, usernameField, passwordField, loginButton, errorLabel);
        root.setPadding(new Insets(30));
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 400, 300);
        stage.setTitle("Admin Login");
        stage.setScene(scene);
        stage.show();
    }
}
