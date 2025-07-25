package crud;

import dashboard.CandidateDashboard;
import db.Database;
import util.PasswordUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CandidateLogin {
    public void showLoginScreen(Stage stage) {
        VBox root = new VBox(15);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("Candidate Login");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        TextField nationalIdField = new TextField();
        nationalIdField.setPromptText("Enter National ID");
        nationalIdField.setMaxWidth(250);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        passwordField.setMaxWidth(250);

        Button loginBtn = new Button("Login");
        loginBtn.setDefaultButton(true);
        loginBtn.setStyle("-fx-background-color: #3f51b5; -fx-text-fill: white; -fx-font-weight: bold;");

        loginBtn.setOnAction(e -> {
            String nid = nationalIdField.getText().trim();
            String password = passwordField.getText().trim();

            if (nid.isEmpty() || password.isEmpty()) {
                showAlert("Please fill in all fields.");
                return;
            }

            if (authenticate(nid, password)) {
                showAlert("Login Successful!");
                stage.close(); // ✅ Close the login window
                CandidateDashboard.show(nid); // ✅ Open new dashboard
            } else {
                showAlert("Invalid credentials.");
            }
        });

        root.getChildren().addAll(title, nationalIdField, passwordField, loginBtn);

        Scene scene = new Scene(root, 350, 250);
        stage.setScene(scene);
        stage.setTitle("Candidate Login");
        stage.show();
    }

    private boolean authenticate(String nid, String password) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT password FROM candidates WHERE national_id = ?");
            stmt.setString(1, nid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password");
                return PasswordUtil.checkPassword(password, storedHash);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
