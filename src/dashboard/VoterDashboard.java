package dashboard;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import db.Database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class VoterDashboard {

    private String fullName;

    public VoterDashboard(String fullName, String nationalId) {
        this.fullName = fullName;
    }

    public void showDashboard() {
        Stage stage = new Stage();
        stage.setTitle("Voter Dashboard");

        // Greeting
        Label greeting = new Label("👋 Hello, " + fullName + "!");
        greeting.setFont(new Font(28));
        greeting.setStyle("-fx-font-weight: bold;");
        greeting.setPadding(new Insets(20));

        VBox candidatesBox = new VBox(15);
        candidatesBox.setPadding(new Insets(20));

        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM candidates")) {

            while (rs.next()) {
                VBox card = new VBox(5);
                card.setPadding(new Insets(10));
                card.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: #f9f9f9;");
                card.setMaxWidth(500);

                String name = rs.getString("full_name");
                String party = rs.getString("party");
                String vision = rs.getString("vision");
                String milestone = rs.getString("milestone");
                String imagePath = rs.getString("photo_path");

                Label nameLabel = new Label(name);
                nameLabel.setFont(new Font(20));
                nameLabel.setStyle("-fx-font-weight: bold;");

                VBox bulletPoints = new VBox(
                        new Label("• Party: " + party),
                        new Label("• Vision: " + vision),
                        new Label("• Milestone: " + milestone)
                );

                ImageView profileImage = new ImageView();
                try {
                    profileImage.setImage(new Image("file:" + imagePath));
                    profileImage.setFitWidth(120);
                    profileImage.setFitHeight(120);
                    profileImage.setPreserveRatio(true);
                    profileImage.setStyle("-fx-border-color: black; -fx-border-width: 1;");
                } catch (Exception e) {
                    System.out.println("Failed to load image: " + imagePath);
                }

                HBox candidateRow = new HBox(15, profileImage, new VBox(nameLabel, bulletPoints));
                card.getChildren().add(candidateRow);
                candidatesBox.getChildren().add(card);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        VBox root = new VBox(greeting, candidatesBox);
        root.setPadding(new Insets(30));
        Scene scene = new Scene(root, 900, 600);

        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
