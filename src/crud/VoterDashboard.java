package crud;

import db.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Optional;

public class VoterDashboard {
    private String fullName;
    private String nationalId;

    public VoterDashboard(String fullName, String nationalId) {
        this.fullName = fullName;
        this.nationalId = nationalId;
    }

    public void show(Stage stage) {
        // Top bar with greeting and buttons
        Label greeting = new Label("👋 Hello, " + fullName + "!");
        greeting.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #333;");

        Button logoutButton = new Button("🚪 Logout");
        logoutButton.setOnAction(e -> new VoterLogin().show(stage));
        logoutButton.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");

        Button refreshButton = new Button("🔄 Refresh");
        refreshButton.setOnAction(e -> show(stage));
        refreshButton.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");

        HBox topBar = new HBox(20, greeting, refreshButton, logoutButton);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(10, 0, 10, 0));
        HBox.setHgrow(greeting, Priority.ALWAYS);
        HBox.setMargin(logoutButton, new Insets(0, 0, 0, 300)); // Pushes logout button to the right

        // Candidate List Container
        VBox candidateList = new VBox(25);
        candidateList.setPadding(new Insets(20));
        candidateList.setStyle("-fx-background-color: #f0f2f5;");
        candidateList.setAlignment(Pos.TOP_CENTER); // Center the cards

        boolean hasCandidates = false;

        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM candidates WHERE approved = 1");

            if (!rs.isBeforeFirst()) {
                Label noCandidatesLabel = new Label("📭 No approved candidates available. Check back later.");
                noCandidatesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #888;");
                candidateList.getChildren().add(noCandidatesLabel);
            }

            while (rs.next()) {
                hasCandidates = true;

                String name = rs.getString("full_name");
                String party = rs.getString("party");
                String position = rs.getString("position");
                String manifesto = rs.getString("manifesto");
                String photoPath = rs.getString("photo_path");
                String candidateId = rs.getString("national_id");
                
                String clearanceCertPath = rs.getString("clearance_certificate_path");
                String academicCertPath = rs.getString("academic_cert_path");

                // Candidate Card
                VBox card = new VBox(10);
                card.setPadding(new Insets(15));
                card.setStyle(
                    "-fx-border-color: #e0e0e0; " +
                    "-fx-border-radius: 10; " +
                    "-fx-background-radius: 10; " +
                    "-fx-background-color: #ffffff; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
                );
                card.setMaxWidth(800);

                ImageView profileImage = new ImageView();
                if (photoPath != null && Files.exists(new File(photoPath).toPath())) {
                    try {
                        Image image = new Image(new FileInputStream(photoPath));
                        profileImage.setImage(image);
                        profileImage.setFitWidth(120);
                        profileImage.setFitHeight(120);
                        profileImage.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5;");
                    } catch (IOException e) {
                        System.err.println("Error loading image for candidate: " + name);
                    }
                }

                Label nameLabel = new Label(name);
                nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #007bff;");

                Label positionLabel = new Label(position + " | Party: " + (party != null ? party : "Independent"));
                positionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");

                Label manifestoTitle = new Label("Manifesto:");
                manifestoTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

                TextArea manifestoArea = new TextArea(manifesto != null ? manifesto : "No manifesto provided.");
                manifestoArea.setEditable(false);
                manifestoArea.setWrapText(true);
                manifestoArea.setPrefHeight(100);
                manifestoArea.setStyle("-fx-control-inner-background: #e9ecef; -fx-font-size: 12px; -fx-border-color: transparent;");

                VBox infoBox = new VBox(5, nameLabel, positionLabel, manifestoTitle, manifestoArea);
                
                HBox candidateBox = new HBox(15, profileImage, infoBox);
                candidateBox.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(infoBox, Priority.ALWAYS);

                HBox docButtons = new HBox(10);
                docButtons.setPadding(new Insets(10, 0, 0, 0));
                docButtons.setAlignment(Pos.CENTER_RIGHT);

                Button downloadManifestoBtn = new Button("Download Manifesto");
                downloadManifestoBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
                downloadManifestoBtn.setOnAction(e -> downloadManifesto(stage, name, manifesto));
                docButtons.getChildren().add(downloadManifestoBtn);

                Button downloadClearance = new Button("Download Clearance Cert");
                downloadClearance.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
                downloadClearance.setDisable(clearanceCertPath == null || clearanceCertPath.isEmpty());
                downloadClearance.setOnAction(e -> downloadFile(clearanceCertPath, stage));
                docButtons.getChildren().add(downloadClearance);

                Button downloadAcademic = new Button("Download Academic Cert");
                downloadAcademic.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
                downloadAcademic.setDisable(academicCertPath == null || academicCertPath.isEmpty());
                downloadAcademic.setOnAction(e -> downloadFile(academicCertPath, stage));
                docButtons.getChildren().add(downloadAcademic);

                card.getChildren().addAll(candidateBox, docButtons);
                candidateList.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
            candidateList.getChildren().add(new Label("⚠ Failed to load candidate details."));
        }

        ScrollPane scrollPane = new ScrollPane(candidateList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f2f5; -fx-border-color: transparent;");

        VBox root = new VBox(20, topBar, scrollPane);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f0f2f5;");

        Scene scene = new Scene(root, 1200, 800);
        stage.setTitle("Voter Dashboard");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    private void downloadFile(String sourcePath, Stage stage) {
        if (sourcePath == null || sourcePath.isEmpty()) {
            showAlert("No Document", "The document path is empty.");
            return;
        }

        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            showAlert("File Not Found", "The document file could not be found at the specified path: " + sourcePath);
            return;
        }

        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save File As");
            fileChooser.setInitialFileName(sourceFile.getName());
            File target = fileChooser.showSaveDialog(stage);

            if (target != null) {
                Files.copy(sourceFile.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert("Success", "File saved successfully!");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Error", "An error occurred while saving the file: " + ex.getMessage());
        }
    }

    private void downloadManifesto(Stage stage, String candidateName, String manifestoText) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Manifesto");
        fileChooser.setInitialFileName(candidateName.replaceAll("\\s+", "_") + "_Manifesto.txt");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files (*.txt)", "*.txt"));
        
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (FileWriter fileWriter = new FileWriter(file)) {
                fileWriter.write(manifestoText);
                showAlert("Success", "Manifesto saved successfully!");
            } catch (IOException e) {
                showAlert("Download Error", "An error occurred while saving the manifesto: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}