package crud;

import db.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
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
        HBox.setMargin(logoutButton, new Insets(0, 0, 0, 300));

        VBox contentBox = new VBox(20);
        contentBox.setPadding(new Insets(20));
        contentBox.setStyle("-fx-background-color: #f0f2f5;");
        contentBox.setAlignment(Pos.TOP_CENTER);
        
        VBox electionInfoBox = new VBox(5);
        electionInfoBox.setAlignment(Pos.CENTER);
        Label electionPeriodLabel = new Label();
        Label voteStatusLabel = new Label();
        
        boolean hasVoted = checkHasVoted();
        boolean isVotingActive = false;
        boolean electionEnded = false;
        
        try (Connection conn = Database.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT start_date, end_date FROM election_period ORDER BY id DESC LIMIT 1");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                LocalDate startDate = rs.getDate("start_date").toLocalDate();
                LocalDate endDate = rs.getDate("end_date").toLocalDate();
                LocalDate currentDate = LocalDate.now();

                electionPeriodLabel.setText("Election Period: " + startDate.toString() + " to " + endDate.toString());
                electionPeriodLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");

                if (currentDate.isAfter(endDate)) {
                    electionEnded = true;
                    voteStatusLabel.setText("🗳️ The election period has ended. See the final results below.");
                    voteStatusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
                } else if (currentDate.isBefore(startDate)) {
                    voteStatusLabel.setText("🗓️ The election has not started yet. Please check back on " + startDate.toString() + ".");
                    voteStatusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #ffc107;");
                } else {
                    isVotingActive = true;
                    if (hasVoted) {
                        voteStatusLabel.setText("✅ You have already cast your vote. You can only vote once.");
                        voteStatusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
                    } else {
                        voteStatusLabel.setText("🗳️ The election is currently active. Cast your vote below!");
                        voteStatusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
                    }
                }
            } else {
                voteStatusLabel.setText("⚠ No election period has been set by the admin.");
                voteStatusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
            }
        } catch (Exception e) {
            e.printStackTrace();
            voteStatusLabel.setText("⚠ An error occurred while checking the election period.");
            voteStatusLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dc3545;");
        }
        
        electionInfoBox.getChildren().addAll(electionPeriodLabel, voteStatusLabel);
        contentBox.getChildren().add(electionInfoBox);

        if (electionEnded) {
            contentBox.getChildren().add(getResultsContent());
        } else {
            contentBox.getChildren().add(getCandidateList(stage, isVotingActive, hasVoted));
        }

        ScrollPane scrollPane = new ScrollPane(contentBox);
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
    
    private VBox getResultsContent() {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ffffff;");
        
        Label title = new Label("Final Election Results");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
        container.getChildren().add(title);
        
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement(
                "SELECT c.full_name, c.party, c.position, c.photo_path, COUNT(v.vote_id) as vote_count " +
                "FROM candidates c LEFT JOIN votes v ON c.candidate_id = v.candidate_id " +
                "WHERE c.approved = 1 " +
                "GROUP BY c.candidate_id " +
                "ORDER BY vote_count DESC, c.full_name ASC"
            );
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.isBeforeFirst()) {
                container.getChildren().add(new Label("No votes have been cast in this election."));
            }

            while (rs.next()) {
                String name = rs.getString("full_name");
                String party = rs.getString("party");
                String position = rs.getString("position");
                String photo = rs.getString("photo_path");
                int voteCount = rs.getInt("vote_count");
                
                HBox resultCard = new HBox(20);
                resultCard.setAlignment(Pos.CENTER_LEFT);
                resultCard.setPadding(new Insets(15));
                resultCard.setStyle(
                    "-fx-border-color: #e0e0e0; " +
                    "-fx-border-radius: 10; " +
                    "-fx-background-radius: 10; " +
                    "-fx-background-color: #fafafa; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);"
                );

                ImageView img = new ImageView();
                if (photo != null && Files.exists(Paths.get(photo))) {
                    try {
                        img.setImage(new Image(new FileInputStream(photo)));
                        img.setFitWidth(80);
                        img.setFitHeight(80);
                        img.setPreserveRatio(true);
                        img.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5;");
                    } catch (Exception ex) {
                        System.err.println("Error loading image for candidate: " + name);
                        img.setImage(null);
                    }
                }
                
                VBox detailsBox = new VBox(5);
                Label nameLabel = new Label(name);
                nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
                Label positionLabel = new Label(position + " | Party: " + party);
                positionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
                
                Label votesLabel = new Label("Votes: " + voteCount);
                votesLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #28a745;");
                
                detailsBox.getChildren().addAll(nameLabel, positionLabel, votesLabel);
                resultCard.getChildren().addAll(img, detailsBox);
                container.getChildren().add(resultCard);
            }
        } catch (Exception e) {
            container.getChildren().add(new Label("Error loading results. " + e.getMessage()));
            e.printStackTrace();
        }

        return container;
    }

    private VBox getCandidateList(Stage stage, boolean isVotingActive, boolean hasVoted) {
        VBox candidateList = new VBox(25);
        candidateList.setPadding(new Insets(20));
        candidateList.setStyle("-fx-background-color: #f0f2f5;");
        candidateList.setAlignment(Pos.TOP_CENTER);
        
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM candidates WHERE approved = 1");

            if (!rs.isBeforeFirst()) {
                Label noCandidatesLabel = new Label("📭 No approved candidates available. Check back later.");
                noCandidatesLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #888;");
                candidateList.getChildren().add(noCandidatesLabel);
            }

            while (rs.next()) {
                int candidateId = rs.getInt("candidate_id");
                String name = rs.getString("full_name");
                String party = rs.getString("party");
                String position = rs.getString("position");
                String manifesto = rs.getString("manifesto");
                String photoPath = rs.getString("photo_path");
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
                if (photoPath != null && Files.exists(Paths.get(photoPath))) {
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

                HBox actionButtons = new HBox(10);
                actionButtons.setPadding(new Insets(10, 0, 0, 0));
                actionButtons.setAlignment(Pos.CENTER_RIGHT);

                Button downloadManifestoBtn = new Button("Download Manifesto");
                downloadManifestoBtn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
                downloadManifestoBtn.setOnAction(e -> downloadManifesto(stage, name, manifesto));
                
                Button downloadClearance = new Button("Download Clearance Cert");
                downloadClearance.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
                downloadClearance.setDisable(clearanceCertPath == null || clearanceCertPath.isEmpty());
                downloadClearance.setOnAction(e -> downloadFile(clearanceCertPath, stage));
                
                Button downloadAcademic = new Button("Download Academic Cert");
                downloadAcademic.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
                downloadAcademic.setDisable(academicCertPath == null || academicCertPath.isEmpty());
                downloadAcademic.setOnAction(e -> downloadFile(academicCertPath, stage));
                
                Button voteButton = new Button("🗳️ Vote");
                voteButton.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15; -fx-border-radius: 5; -fx-background-radius: 5;");
                voteButton.setDisable(!isVotingActive || hasVoted);
                voteButton.setOnAction(e -> handleVote(candidateId, name, stage));

                actionButtons.getChildren().addAll(downloadManifestoBtn, downloadClearance, downloadAcademic, voteButton);
                card.getChildren().addAll(candidateBox, actionButtons);
                candidateList.getChildren().add(card);
            }

        } catch (Exception e) {
            e.printStackTrace();
            candidateList.getChildren().add(new Label("⚠ Failed to load candidate details."));
        }
        return candidateList;
    }

    private boolean checkHasVoted() {
        try (Connection conn = Database.getConnection()) {
            // Updated logic to check if the voter has voted in the current active election
            PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM votes v " +
                "JOIN election_period ep ON v.voted_on_date BETWEEN ep.start_date AND ep.end_date " +
                "WHERE v.voter_id = ? AND CURDATE() BETWEEN ep.start_date AND ep.end_date"
            );
            pstmt.setString(1, nationalId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void handleVote(int candidateId, String candidateName, Stage stage) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Vote");
        confirmationAlert.setHeaderText("Casting your vote for " + candidateName);
        confirmationAlert.setContentText("Are you sure you want to vote for this candidate? This action cannot be undone.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            castVote(candidateId, stage);
        }
    }

    private void castVote(int candidateId, Stage stage) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO votes (voter_id, candidate_id) VALUES (?, ?)");
            pstmt.setString(1, nationalId);
            pstmt.setInt(2, candidateId);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                showAlert("Success", "Your vote has been successfully cast!");
                show(stage);
            } else {
                showAlert("Error", "Failed to cast your vote. Please try again.");
            }
        } catch (java.sql.SQLIntegrityConstraintViolationException e) {
            showAlert("Already Voted", "You have already cast your vote. You can only vote once per election.");
            show(stage);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "An unexpected error occurred: " + e.getMessage());
        }
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