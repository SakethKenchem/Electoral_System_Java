package dashboard;

import crud.CandidateLogin;
import db.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.Candidate;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CandidateDashboard {

    public static void show(String nationalId) {
        Stage stage = new Stage();
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        Label heading = new Label("Candidate Dashboard");
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        VBox profileBox = new VBox(15);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.setPadding(new Insets(20));

        ImageView photoView = new ImageView();
        photoView.setFitHeight(120);
        photoView.setFitWidth(120);

        Label nameLabel = new Label();
        Label idLabel = new Label();
        Label partyLabel = new Label();
        Label positionLabel = new Label();
        Label manifestoLabel = new Label();

        profileBox.getChildren().addAll(photoView, nameLabel, idLabel, partyLabel, positionLabel, manifestoLabel);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20));

        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button logoutBtn = new Button("Logout");

        buttonBox.getChildren().addAll(editBtn, deleteBtn, logoutBtn);

        VBox centerBox = new VBox(20, profileBox, buttonBox);
        centerBox.setAlignment(Pos.TOP_CENTER);

        root.setTop(heading);
        BorderPane.setAlignment(heading, Pos.CENTER);
        root.setCenter(centerBox);

        // Load candidate data
        Candidate loggedInCandidate = null;
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM candidates WHERE national_id = ?");
            stmt.setString(1, nationalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                loggedInCandidate = new Candidate(
                        rs.getString("full_name"),
                        rs.getString("national_id"),
                        rs.getString("party"),
                        rs.getString("position"),
                        rs.getString("manifesto"),
                        rs.getString("password"),
                        rs.getString("national_id_path"),
                        rs.getString("clearance_certificate_path"),
                        rs.getString("academic_cert_path"),
                        rs.getString("photo_path")
                );

                nameLabel.setText("Name: " + loggedInCandidate.getFullName());
                idLabel.setText("ID: " + loggedInCandidate.getNationalId());
                partyLabel.setText("Party: " + loggedInCandidate.getParty());
                positionLabel.setText("Position: " + loggedInCandidate.getPosition());
                manifestoLabel.setText("Manifesto: " + loggedInCandidate.getManifesto());

                File photoFile = new File(loggedInCandidate.getPhotoPath());
                if (photoFile.exists()) {
                    photoView.setImage(new Image(photoFile.toURI().toString()));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Candidate finalLoggedInCandidate = loggedInCandidate;

        // Edit functionality
        editBtn.setOnAction(e -> {
            if (finalLoggedInCandidate == null) return;
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);

            VBox box = new VBox(10);
            box.setPadding(new Insets(20));

            TextField nameField = new TextField(finalLoggedInCandidate.getFullName());
            TextField partyField = new TextField(finalLoggedInCandidate.getParty());
            TextField positionField = new TextField(finalLoggedInCandidate.getPosition());

            FileChooser fileChooser = new FileChooser();
            Button selectManifestoBtn = new Button("Select Manifesto PDF");
            Label manifestoPathLabel = new Label(finalLoggedInCandidate.getManifesto());

            selectManifestoBtn.setOnAction(ev -> {
                File selectedFile = fileChooser.showOpenDialog(dialog);
                if (selectedFile != null) {
                    manifestoPathLabel.setText(selectedFile.getAbsolutePath());
                }
            });

            Button updateBtn = new Button("Update");
            updateBtn.setOnAction(ev -> {
                try (Connection conn = Database.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("UPDATE candidates SET full_name = ?, party = ?, position = ?, manifesto = ? WHERE national_id = ?");
                    stmt.setString(1, nameField.getText());
                    stmt.setString(2, partyField.getText());
                    stmt.setString(3, positionField.getText());
                    stmt.setString(4, manifestoPathLabel.getText());
                    stmt.setString(5, finalLoggedInCandidate.getNationalId());
                    stmt.executeUpdate();

                    finalLoggedInCandidate.setFullName(nameField.getText());
                    finalLoggedInCandidate.setParty(partyField.getText());
                    finalLoggedInCandidate.setPosition(positionField.getText());
                    finalLoggedInCandidate.setManifesto(manifestoPathLabel.getText());

                    nameLabel.setText("Name: " + finalLoggedInCandidate.getFullName());
                    partyLabel.setText("Party: " + finalLoggedInCandidate.getParty());
                    positionLabel.setText("Position: " + finalLoggedInCandidate.getPosition());
                    manifestoLabel.setText("Manifesto: " + finalLoggedInCandidate.getManifesto());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                dialog.close();
            });

            box.getChildren().addAll(
                new Label("Full Name:"), nameField,
                new Label("Party:"), partyField,
                new Label("Position:"), positionField,
                new Label("Manifesto File:"), selectManifestoBtn, manifestoPathLabel,
                updateBtn
            );

            dialog.setScene(new Scene(box));
            dialog.setTitle("Edit Profile");
            dialog.show();
        });

        // Delete functionality
        deleteBtn.setOnAction(e -> {
            if (finalLoggedInCandidate == null) return;
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete your profile?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();

            if (confirm.getResult() == ButtonType.YES) {
                try (Connection conn = Database.getConnection()) {
                    PreparedStatement stmt = conn.prepareStatement("DELETE FROM candidates WHERE national_id = ?");
                    stmt.setString(1, finalLoggedInCandidate.getNationalId());
                    stmt.executeUpdate();
                    stage.close();
                    new CandidateLogin().showLoginScreen(new Stage());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // Logout functionality
        logoutBtn.setOnAction(e -> {
            stage.close();
            new CandidateLogin().showLoginScreen(new Stage());
        });

        stage.setScene(new Scene(root, 600, 500));
        stage.setTitle("Candidate Dashboard");
        stage.show();
    }
}
