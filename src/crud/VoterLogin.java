package crud;

import db.Database;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VoterLogin extends Application {

    @Override
    public void start(Stage stage) {
        Label title = new Label("🗳️ Voter Login");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Label idLabel = new Label("National ID:");
        TextField idField = new TextField();
        idField.setPromptText("Enter your National ID");

        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Label message = new Label();
        message.setStyle("-fx-text-fill: red;");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String nationalId = idField.getText().trim();
            String password = passwordField.getText().trim();

            if (nationalId.isEmpty() || password.isEmpty()) {
                message.setText("Please fill in all fields.");
                return;
            }

            try (Connection conn = Database.getConnection()) {
                String sql = "SELECT * FROM voters WHERE national_id = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nationalId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    String storedHash = rs.getString("password");
                    String fullName = rs.getString("full_name");

                    if (PasswordUtil.checkPassword(password, storedHash)) {
                        VoterDashboard dashboard = new VoterDashboard(fullName, nationalId);
                        dashboard.show(stage);
                    } else {
                        message.setText("Invalid password.");
                    }
                } else {
                    message.setText("Voter not found.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                message.setText("Login failed due to an error.");
            }
        });

        VBox form = new VBox(10, title, idLabel, idField, passwordLabel, passwordField, loginButton, message);
        form.setPadding(new Insets(30));
        form.setAlignment(Pos.CENTER);

        Scene scene = new Scene(form, 500, 400);
        stage.setTitle("Voter Login");
        stage.setScene(scene);
        stage.show();
    }

    public static void show(Stage stage) {
        try {
            new VoterLogin().start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
