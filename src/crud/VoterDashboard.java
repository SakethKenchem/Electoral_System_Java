package crud;

import db.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class VoterDashboard {
    private final String fullName;
    private final String nationalId;

    public VoterDashboard(String fullName, String nationalId) {
        this.fullName = fullName;
        this.nationalId = nationalId;
    }

    public void show(Stage stage) {
        Label greeting = new Label("👋 Hello, " + fullName + "!");
        greeting.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        VBox candidateList = new VBox(20);
        candidateList.setPadding(new Insets(20));

        Label emptyMessage = new Label("✅ No approved candidates available at the moment.\n🔁 Please check back later.");
        emptyMessage.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
        emptyMessage.setVisible(false);

        loadCandidates(candidateList, emptyMessage);

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setOnAction(e -> {
            candidateList.getChildren().clear();
            emptyMessage.setVisible(false);
            loadCandidates(candidateList, emptyMessage);
        });

        Button logoutButton = new Button("🚪 Logout");
        logoutButton.setStyle("-fx-font-size: 14px;");
        logoutButton.setOnAction(e -> {
            VoterLogin.show(stage); // Go back to login
        });

        HBox topBar = new HBox(20, greeting, refreshButton, logoutButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(candidateList);
        scrollPane.setFitToWidth(true);

        VBox root = new VBox(20, topBar, scrollPane, emptyMessage);
        root.setPadding(new Insets(20));

        Scene scene = new Scene(root, 1000, 700);
        stage.setTitle("Voter Dashboard");
        stage.setScene(scene);
        stage.setFullScreen(true);
        stage.show();
    }

    private void loadCandidates(VBox candidateList, Label emptyMessage) {
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM candidates WHERE approved = 1");

            boolean hasResults = false;
            while (rs.next()) {
                hasResults = true;
                String name = rs.getString("full_name");
                String party = rs.getString("party");
                String position = rs.getString("position");
                String agenda = rs.getString("manifesto");
                String photoPath = rs.getString("photo_path");

                Label nameLabel = new Label(name + " (" + position + ")");
                nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

                String bulletDetails = String.format("• Party: %s\n• Manifesto:\n• %s",
                        party != null ? party : "Independent",
                        agenda != null ? agenda.replace("\n", "\n• ") : "No manifesto");

                Label detailsLabel = new Label(bulletDetails);
                detailsLabel.setStyle("-fx-font-size: 14px;");

                ImageView profileImage = new ImageView();
                if (photoPath != null) {
                    File imgFile = new File(photoPath);
                    if (imgFile.exists()) {
                        Image image = new Image(new FileInputStream(imgFile));
                        profileImage.setImage(image);
                        profileImage.setFitWidth(120);
                        profileImage.setFitHeight(120);
                        profileImage.setStyle("-fx-border-color: black; -fx-border-width: 2px;");
                    }
                }

                HBox candidateBox = new HBox(15, profileImage, new VBox(nameLabel, detailsLabel));
                candidateBox.setAlignment(Pos.CENTER_LEFT);
                candidateBox.setPadding(new Insets(10));
                candidateBox.setStyle("-fx-border-color: gray; -fx-border-width: 1px; -fx-background-color: #f9f9f9;");

                candidateList.getChildren().add(candidateBox);
            }

            if (!hasResults) {
                candidateList.getChildren().add(emptyMessage);
                emptyMessage.setVisible(true);
            }

        } catch (Exception e) {
            e.printStackTrace();
            candidateList.getChildren().add(new Label("⚠ Failed to load candidate details."));
        }
    }
}
