package crud;

import crud.VoterLogin;
import db.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import util.PasswordUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class VoterRegister {
    public void start(Stage stage) {
        Label title = new Label("Voter Registration");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField nationalIdField = new TextField();
        nationalIdField.setPromptText("National ID");

        TextField fullNameField = new TextField();
        fullNameField.setPromptText("Full Name");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Label idCardLabel = new Label("Upload ID Card");
        Button idCardButton = new Button("Choose File");
        Label idCardPathLabel = new Label();

        Label voterCardLabel = new Label("Upload Voter Card");
        Button voterCardButton = new Button("Choose File");
        Label voterCardPathLabel = new Label();

        FileChooser fileChooser = new FileChooser();

        final File[] idCardFile = new File[1];
        final File[] voterCardFile = new File[1];

        idCardButton.setOnAction(e -> {
            idCardFile[0] = fileChooser.showOpenDialog(stage);
            if (idCardFile[0] != null) idCardPathLabel.setText(idCardFile[0].getName());
        });

        voterCardButton.setOnAction(e -> {
            voterCardFile[0] = fileChooser.showOpenDialog(stage);
            if (voterCardFile[0] != null) voterCardPathLabel.setText(voterCardFile[0].getName());
        });

        Button registerButton = new Button("Register");
        Button loginInstead = new Button("Already have an account? Login");

        registerButton.setOnAction(e -> {
            try {
                String nationalId = nationalIdField.getText();
                String fullName = fullNameField.getText();
                String password = passwordField.getText();
                if (nationalId.isEmpty() || fullName.isEmpty() || password.isEmpty() || idCardFile[0] == null || voterCardFile[0] == null) {
                    showAlert("Error", "Please fill all fields and select files");
                    return;
                }
                String hashedPassword = PasswordUtil.hashPassword(password);
                File folder = new File("voter_docs/" + nationalId);
                folder.mkdirs();
                File idCardDest = new File(folder, "id_card.png");
                File voterCardDest = new File(folder, "voter_card.png");
                Files.copy(idCardFile[0].toPath(), idCardDest.toPath());
                Files.copy(voterCardFile[0].toPath(), voterCardDest.toPath());
                Connection conn = Database.getConnection();
                PreparedStatement stmt = conn.prepareStatement("INSERT INTO voters (national_id, full_name, password, id_card_path, voter_card_path) VALUES (?, ?, ?, ?, ?)");
                stmt.setString(1, nationalId);
                stmt.setString(2, fullName);
                stmt.setString(3, hashedPassword);
                stmt.setString(4, idCardDest.getPath());
                stmt.setString(5, voterCardDest.getPath());
                stmt.executeUpdate();
                conn.close();
                showAlert("Success", "Voter registered successfully");
                new VoterLogin().start(stage);
            } catch (Exception ex) {
                ex.printStackTrace();
                showAlert("Error", "Failed to register voter");
            }
        });

        loginInstead.setOnAction(e -> new VoterLogin().start(stage));

        VBox root = new VBox(10, title, nationalIdField, fullNameField, passwordField,
                idCardLabel, idCardButton, idCardPathLabel,
                voterCardLabel, voterCardButton, voterCardPathLabel,
                registerButton, loginInstead);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        stage.setScene(new Scene(root, 600, 600));
        stage.setTitle("Voter Registration");
        stage.show();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
