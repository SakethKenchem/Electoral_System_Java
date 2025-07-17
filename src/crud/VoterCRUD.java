package crud;

import db.Database;
import util.PasswordUtil;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class VoterCRUD {

    private VBox registerView;

    public VoterCRUD(Stage stage) {
        registerView = new VBox(12);
        registerView.setPadding(new Insets(20));
        registerView.setAlignment(Pos.CENTER);

        Label title = new Label("Voter Registration");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");

        TextField nationalIdField = new TextField();
        nationalIdField.setPromptText("National ID");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label idLabel = new Label("Upload ID Card:");
        Button uploadIdButton = new Button("Choose ID Card");
        Label idFileLabel = new Label();

        Label voterLabel = new Label("Upload Voter Card:");
        Button uploadVoterButton = new Button("Choose Voter Card");
        Label voterFileLabel = new Label();

        Button registerButton = new Button("Register");
        Button loginButton = new Button("Already have an account? Login");

        Label messageLabel = new Label();

        final File[] idCardFile = {null};
        final File[] voterCardFile = {null};

        uploadIdButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select ID Card");
            idCardFile[0] = fileChooser.showOpenDialog(stage);
            if (idCardFile[0] != null) {
                idFileLabel.setText(idCardFile[0].getName());
            }
        });

        uploadVoterButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select Voter Card");
            voterCardFile[0] = fileChooser.showOpenDialog(stage);
            if (voterCardFile[0] != null) {
                voterFileLabel.setText(voterCardFile[0].getName());
            }
        });

        registerButton.setOnAction(e -> {
            String fullName = fullNameField.getText().trim();
            String nationalId = nationalIdField.getText().trim();
            String password = passwordField.getText().trim();

            if (fullName.isEmpty() || nationalId.isEmpty() || password.isEmpty()
                    || idCardFile[0] == null || voterCardFile[0] == null) {
                messageLabel.setText("All fields and uploads are required.");
                return;
            }

            try {
                // Create directory for this voter
                File voterDir = new File("voter_documents/" + nationalId);
                if (!voterDir.exists()) {
                    voterDir.mkdirs();
                }

                // Copy ID card
                File idDest = new File(voterDir, "id_card" + getFileExtension(idCardFile[0]));
                Files.copy(idCardFile[0].toPath(), idDest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Copy voter card
                File voterDest = new File(voterDir, "voter_card" + getFileExtension(voterCardFile[0]));
                Files.copy(voterCardFile[0].toPath(), voterDest.toPath(), StandardCopyOption.REPLACE_EXISTING);

                String hashedPassword = PasswordUtil.hashPassword(password);

                try (Connection conn = Database.getConnection()) {
                    String sql = "INSERT INTO voters (national_id, full_name, password, id_card_path, voter_card_path) " +
                            "VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setString(1, nationalId);
                    stmt.setString(2, fullName);
                    stmt.setString(3, hashedPassword);
                    stmt.setString(4, idDest.getAbsolutePath());
                    stmt.setString(5, voterDest.getAbsolutePath());

                    stmt.executeUpdate();
                    messageLabel.setText("Registration successful! Redirecting...");

                    // 2-second pause then redirect
                    new Thread(() -> {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                        Platform.runLater(() -> {
                            VoterLogin login = new VoterLogin(stage);
                            Scene loginScene = new Scene(login.getView(), 600, 400);
                            stage.setScene(loginScene);
                        });
                    }).start();

                } catch (Exception dbEx) {
                    dbEx.printStackTrace();
                    messageLabel.setText("Error: Could not register.");
                }

            } catch (IOException ioEx) {
                ioEx.printStackTrace();
                messageLabel.setText("File upload error.");
            }
        });

        loginButton.setOnAction(e -> {
            VoterLogin login = new VoterLogin(stage);
            Scene loginScene = new Scene(login.getView(), 600, 400);
            stage.setScene(loginScene);
        });

        registerView.getChildren().addAll(
                title,
                fullNameField,
                nationalIdField,
                passwordField,
                idLabel, uploadIdButton, idFileLabel,
                voterLabel, uploadVoterButton, voterFileLabel,
                registerButton, loginButton,
                messageLabel
        );
    }

    public VBox getView() {
        return registerView;
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastDot = name.lastIndexOf('.');
        return (lastDot != -1) ? name.substring(lastDot) : "";
    }
}
