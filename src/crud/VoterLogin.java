package crud;

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

public class VoterLogin {

    private VBox loginView;

    public VoterLogin(Stage stage) {
        loginView = new VBox(15);
        loginView.setPadding(new Insets(20));
        loginView.setAlignment(Pos.CENTER);

        Label title = new Label("Voter Login");

        TextField nationalIdField = new TextField();
        nationalIdField.setPromptText("National ID");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginBtn = new Button("Login");
        Label messageLabel = new Label();

        loginBtn.setOnAction(e -> {
            String nationalId = nationalIdField.getText().trim();
            String password = passwordField.getText().trim();

            if (nationalId.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Please fill in all fields.");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                String query = "SELECT * FROM voters WHERE national_id = ?";
                PreparedStatement stmt = conn.prepareStatement(query);
                stmt.setString(1, nationalId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    if (PasswordUtil.checkPassword(password, storedHash)) {
                        messageLabel.setText("Login successful!");

                        // Redirect to dashboard
                        VoterDashboard dashboard = new VoterDashboard(stage,
                                rs.getString("full_name"), rs.getString("national_id"));
                        Scene dashboardScene = new Scene(dashboard.getView(), 600, 400);
                        stage.setScene(dashboardScene);

                    } else {
                        messageLabel.setText("Incorrect password.");
                    }
                } else {
                    messageLabel.setText("Voter not found.");
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                messageLabel.setText("Database error.");
            }
        });

        loginView.getChildren().addAll(title, nationalIdField, passwordField, loginBtn, messageLabel);
    }

    public VBox getView() {
        return loginView;
    }
}
