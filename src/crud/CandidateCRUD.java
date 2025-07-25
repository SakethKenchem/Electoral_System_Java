package crud;

import db.Database;
import model.Candidate;
import util.PasswordUtil;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class CandidateCRUD {

    public void showCandidateRegistration(Stage stage) {
        VBox root = new VBox(20);
        root.setPadding(new Insets(25));
        root.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Candidate Registration");
        titleLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);

        TextField nameField = new TextField();
        TextField nationalIdField = new TextField();
        TextField partyField = new TextField();
        TextField positionField = new TextField();
        PasswordField passwordField = new PasswordField();

        nameField.setPromptText("Full Name");
        nationalIdField.setPromptText("National ID");
        partyField.setPromptText("Party");
        positionField.setPromptText("Position");
        passwordField.setPromptText("Password");

        form.add(new Label("Full Name:"), 0, 0);
        form.add(nameField, 1, 0);
        form.add(new Label("National ID:"), 0, 1);
        form.add(nationalIdField, 1, 1);
        form.add(new Label("Party:"), 0, 2);
        form.add(partyField, 1, 2);
        form.add(new Label("Position:"), 0, 3);
        form.add(positionField, 1, 3);
        form.add(new Label("Password:"), 0, 4);
        form.add(passwordField, 1, 4);

        // File upload controls
        File[] uploads = new File[5]; // ID, clearance, academic, photo, manifesto
        Label[] fileLabels = new Label[5];
        String[] filePrompts = {
            "Upload National ID", "Upload Clearance Certificate", "Upload Academic Certificate",
            "Upload Profile Photo", "Upload Manifesto (PDF)"
        };

        VBox fileUploadBox = new VBox(12);
        for (int i = 0; i < 5; i++) {
            HBox fileRow = new HBox(10);
            fileRow.setAlignment(Pos.CENTER_LEFT);
            Button uploadBtn = new Button(filePrompts[i]);
            Label fileLabel = new Label("No file chosen");
            fileLabel.setPrefWidth(250);
            fileLabels[i] = fileLabel;

            final int index = i;
            uploadBtn.setOnAction(e -> uploads[index] = chooseFile(stage, fileLabel));

            fileRow.getChildren().addAll(uploadBtn, fileLabel);
            fileUploadBox.getChildren().add(fileRow);
        }

        Button registerBtn = new Button("Register Candidate");
        registerBtn.setStyle("-fx-background-color: #388e3c; -fx-text-fill: white;");
        Button loginBtn = new Button("Already have an account? Login");

        registerBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String nid = nationalIdField.getText().trim();
            String party = partyField.getText().trim();
            String position = positionField.getText().trim();
            String passwordRaw = passwordField.getText().trim();

            if (name.isEmpty() || nid.isEmpty() || party.isEmpty() || position.isEmpty() || passwordRaw.isEmpty()) {
                showAlert("Please fill all the text fields.");
                return;
            }

            for (int i = 0; i < uploads.length; i++) {
                if (uploads[i] == null) {
                    showAlert("Please upload all required documents.");
                    return;
                }
            }

            String folderPath = "candidate_docs/" + nid;
            new File(folderPath).mkdirs();

            try {
                String idPath = copyFile(uploads[0], folderPath);
                String clearancePath = copyFile(uploads[1], folderPath);
                String academicPath = copyFile(uploads[2], folderPath);
                String photoPath = copyFile(uploads[3], folderPath);
                String manifestoPath = copyFile(uploads[4], folderPath);
                String hashedPassword = PasswordUtil.hashPassword(passwordRaw);

                Candidate candidate = new Candidate(name, nid, party, position, "", hashedPassword,
                        idPath, clearancePath, academicPath, photoPath);

                saveToDatabase(candidate, manifestoPath);
                showAlert("Registration successful! Redirecting to login...");

                PauseTransition pause = new PauseTransition(Duration.seconds(2));
                pause.setOnFinished(ev -> new CandidateLogin().showLoginScreen(stage));
                pause.play();

            } catch (IOException ex) {
                showAlert("File error: " + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Unexpected error occurred.");
            }
        });

        loginBtn.setOnAction(e -> new CandidateLogin().showLoginScreen(stage));

        VBox buttons = new VBox(10, registerBtn, loginBtn);
        buttons.setAlignment(Pos.CENTER);

        root.getChildren().addAll(titleLabel, form, fileUploadBox, buttons);

        Scene scene = new Scene(root, 700, 600);
        stage.setScene(scene);
        stage.setTitle("Candidate Registration");
        stage.show();
    }

    private File chooseFile(Stage stage, Label labelToUpdate) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select File");
        File file = fc.showOpenDialog(stage);
        if (file != null) {
            labelToUpdate.setText("Selected: " + file.getName());
        } else {
            labelToUpdate.setText("No file chosen");
        }
        return file;
    }

    private String copyFile(File source, String folderPath) throws IOException {
        File dest = new File(folderPath, source.getName());
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return dest.getPath();
    }

    private void saveToDatabase(Candidate c, String manifestoPath) throws Exception {
        Connection conn = Database.getConnection();
        String sql = "INSERT INTO candidates (full_name, national_id, party, position, manifesto, password, " +
                "national_id_path, clearance_certificate_path, academic_cert_path, photo_path) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, c.getFullName());
        stmt.setString(2, c.getNationalId());
        stmt.setString(3, c.getParty());
        stmt.setString(4, c.getPosition());
        stmt.setString(5, manifestoPath);
        stmt.setString(6, c.getPassword());
        stmt.setString(7, c.getNationalIdPath());
        stmt.setString(8, c.getClearanceCertificatePath());
        stmt.setString(9, c.getAcademicCertPath());
        stmt.setString(10, c.getPhotoPath());

        stmt.executeUpdate();
        conn.close();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(msg);
        alert.show();
    }
}
