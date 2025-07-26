package crud;

import db.Database;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.PasswordUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class EditCandidateProfile {

    public void showEditModal(String nationalId, Stage parentStage) {
        Stage modal = new Stage();
        modal.initOwner(parentStage);
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Edit Profile");

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField partyField = new TextField();
        TextField positionField = new TextField();
        PasswordField passwordField = new PasswordField();

        Button uploadIDBtn = new Button("Upload New ID");
        Button uploadVoterBtn = new Button("Upload New Voter Card");

        Label idLabel = new Label("No new ID uploaded");
        Label voterLabel = new Label("No new voter card uploaded");

        File[] newFiles = new File[2]; // [0] = ID, [1] = Voter Card

        // File upload handlers
        uploadIDBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File selected = fc.showOpenDialog(modal);
            if (selected != null) {
                newFiles[0] = selected;
                idLabel.setText(selected.getName());
            }
        });

        uploadVoterBtn.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            File selected = fc.showOpenDialog(modal);
            if (selected != null) {
                newFiles[1] = selected;
                voterLabel.setText(selected.getName());
            }
        });

        // Load current details
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM candidates WHERE national_id = ?");
            stmt.setString(1, nationalId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("name"));
                partyField.setText(rs.getString("party"));
                positionField.setText(rs.getString("position"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button saveBtn = new Button("Save Changes");
        saveBtn.setOnAction(e -> {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE candidates SET name=?, party=?, position=?"
                        + (passwordField.getText().isEmpty() ? "" : ", password=?")
                        + (newFiles[0] != null ? ", id_card_path=?" : "")
                        + (newFiles[1] != null ? ", voter_card_path=?" : "")
                        + " WHERE national_id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setString(1, nameField.getText());
                stmt.setString(2, partyField.getText());
                stmt.setString(3, positionField.getText());

                int index = 4;

                if (!passwordField.getText().isEmpty()) {
                    stmt.setString(index++, PasswordUtil.hashPassword(passwordField.getText()));
                }
                if (newFiles[0] != null) {
                    String idPath = saveFile(newFiles[0], nationalId);
                    stmt.setString(index++, idPath);
                }
                if (newFiles[1] != null) {
                    String voterPath = saveFile(newFiles[1], nationalId);
                    stmt.setString(index++, voterPath);
                }

                stmt.setString(index, nationalId); // last param
                stmt.executeUpdate();

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Profile updated!");
                alert.showAndWait();
                modal.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        root.getChildren().addAll(
                new Label("Edit Profile"),
                new Label("Name:"), nameField,
                new Label("Party:"), partyField,
                new Label("Position:"), positionField,
                new Label("Change Password (optional):"), passwordField,
                uploadIDBtn, idLabel,
                uploadVoterBtn, voterLabel,
                saveBtn
        );

        modal.setScene(new Scene(root, 350, 500));
        modal.show();
    }

    private String saveFile(File source, String nationalId) throws IOException {
        File destFolder = new File("uploads/candidates/" + nationalId);
        if (!destFolder.exists()) destFolder.mkdirs();

        File dest = new File(destFolder, source.getName());
        Files.copy(source.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return dest.getPath();
    }
}
