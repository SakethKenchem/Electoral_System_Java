package dashboard;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class CandidateProfileUpload extends Application {

    private TextField nameField, nationalIdField, partyField, positionField;
    private TextArea manifestoArea;

    private Label idPathLabel, clearancePathLabel, academicPathLabel, photoPathLabel;
    private File idFile, clearanceFile, academicFile, photoFile;

    // Change this path to your preferred storage directory
    private final String baseStorageDir = "candidate_docs";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Candidate Profile Upload");

        nameField = new TextField();
        nationalIdField = new TextField();
        partyField = new TextField();
        positionField = new TextField();
        manifestoArea = new TextArea();
        manifestoArea.setPrefRowCount(4);

        Button uploadIdBtn = new Button("Upload National ID");
        Button uploadClearanceBtn = new Button("Upload Clearance Certificate");
        Button uploadAcademicBtn = new Button("Upload Academic Certificate");
        Button uploadPhotoBtn = new Button("Upload Photo");

        idPathLabel = new Label("No file selected");
        clearancePathLabel = new Label("No file selected");
        academicPathLabel = new Label("No file selected");
        photoPathLabel = new Label("No file selected");

        uploadIdBtn.setOnAction(e -> idFile = chooseFile(idPathLabel));
        uploadClearanceBtn.setOnAction(e -> clearanceFile = chooseFile(clearancePathLabel));
        uploadAcademicBtn.setOnAction(e -> academicFile = chooseFile(academicPathLabel));
        uploadPhotoBtn.setOnAction(e -> photoFile = chooseFile(photoPathLabel));

        Button submitBtn = new Button("Submit Profile");
        submitBtn.setOnAction(e -> submitProfile());

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;
        grid.add(new Label("Full Name:"), 0, row); grid.add(nameField, 1, row++);
        grid.add(new Label("National ID:"), 0, row); grid.add(nationalIdField, 1, row++);
        grid.add(new Label("Party:"), 0, row); grid.add(partyField, 1, row++);
        grid.add(new Label("Position:"), 0, row); grid.add(positionField, 1, row++);
        grid.add(new Label("Manifesto:"), 0, row); grid.add(manifestoArea, 1, row++);

        grid.add(uploadIdBtn, 0, row); grid.add(idPathLabel, 1, row++);
        grid.add(uploadClearanceBtn, 0, row); grid.add(clearancePathLabel, 1, row++);
        grid.add(uploadAcademicBtn, 0, row); grid.add(academicPathLabel, 1, row++);
        grid.add(uploadPhotoBtn, 0, row); grid.add(photoPathLabel, 1, row++);

        VBox root = new VBox(15, grid, submitBtn);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        primaryStage.setScene(new Scene(root, 600, 600));
        primaryStage.show();
    }

    private File chooseFile(Label label) {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            label.setText(selectedFile.getName());
        }
        return selectedFile;
    }

    private void submitProfile() {
        String name = nameField.getText().trim();
        String nationalId = nationalIdField.getText().trim();
        String party = partyField.getText().trim();
        String position = positionField.getText().trim();
        String manifesto = manifestoArea.getText().trim();

        if (name.isEmpty() || nationalId.isEmpty() || position.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Name, National ID, and Position are required.");
            return;
        }

        // Create folder for the candidate
        File candidateFolder = new File(baseStorageDir + File.separator + nationalId);
        if (!candidateFolder.exists()) candidateFolder.mkdirs();

        try {
            String idPath = copyFile(idFile, candidateFolder);
            String clearancePath = copyFile(clearanceFile, candidateFolder);
            String academicPath = copyFile(academicFile, candidateFolder);
            String photoPath = copyFile(photoFile, candidateFolder);

            try (Connection conn = connectToDatabase()) {
                String sql = "INSERT INTO candidates (full_name, national_id, party, manifesto, position, approved, " +
                        "national_id_path, clearance_certificate_path, academic_cert_path, photo_path) " +
                        "VALUES (?, ?, ?, ?, ?, FALSE, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, nationalId);
                stmt.setString(3, party);
                stmt.setString(4, manifesto);
                stmt.setString(5, position);
                stmt.setString(6, idPath);
                stmt.setString(7, clearancePath);
                stmt.setString(8, academicPath);
                stmt.setString(9, photoPath);

                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Candidate profile submitted successfully.");
                    clearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Submission failed.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error: " + ex.getMessage());
        }
    }

    private String copyFile(File sourceFile, File destFolder) throws IOException {
        if (sourceFile == null) return null;
        File destFile = new File(destFolder, sourceFile.getName());
        Files.copy(sourceFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        return destFile.getAbsolutePath();
    }

    private void clearForm() {
        nameField.clear();
        nationalIdField.clear();
        partyField.clear();
        positionField.clear();
        manifestoArea.clear();

        idPathLabel.setText("No file selected");
        clearancePathLabel.setText("No file selected");
        academicPathLabel.setText("No file selected");
        photoPathLabel.setText("No file selected");

        idFile = null;
        clearanceFile = null;
        academicFile = null;
        photoFile = null;
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Candidate Upload");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Connection connectToDatabase() throws Exception {
        // Replace with your actual DB connection
        String url = "jdbc:mysql://localhost:3306/electoral_system";
        String user = "root";
        String password = "";
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, user, password);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
