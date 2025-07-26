// AdminDashboard.java
package dashboard;

import crud.AdminLogin;
import db.Database;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Optional;

public class AdminDashboard {
    private String adminUsername;

    public AdminDashboard(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    public void show(Stage stage) {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: #f0f2f5;");

        Tab candidatesTab = new Tab("View Candidates");
        candidatesTab.setContent(getCandidateContent(stage));
        candidatesTab.setClosable(false);
        candidatesTab.setStyle("-fx-font-weight: bold;");

        Tab votersTab = new Tab("View Voters");
        votersTab.setContent(getVoterContent());
        votersTab.setClosable(false);
        votersTab.setStyle("-fx-font-weight: bold;");

        tabPane.getTabs().addAll(candidatesTab, votersTab);

        Button logoutButton = new Button("Logout");
        logoutButton.setStyle(
            "-fx-background-color: #dc3545; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 15; " +
            "-fx-border-radius: 5; " +
            "-fx-background-radius: 5;"
        );
        logoutButton.setOnAction(e -> {
            AdminLogin.show(stage);
        });

        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_RIGHT);
        header.setPadding(new Insets(10));
        Label welcomeLabel = new Label("Logged in as: " + adminUsername);
        welcomeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
        header.getChildren().addAll(welcomeLabel, logoutButton);

        BorderPane root = new BorderPane();
        root.setTop(header);
        root.setCenter(tabPane);
        BorderPane.setMargin(tabPane, new Insets(10));

        Scene scene = new Scene(root, 1000, 600);
        stage.setScene(scene);
        stage.setTitle("Admin Dashboard");
        stage.show();
    }

    private ScrollPane getCandidateContent(Stage stage) {
        VBox container = new VBox(15);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ffffff;");

        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM candidates");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("candidate_id");
                String name = rs.getString("full_name");
                String party = rs.getString("party");
                String position = rs.getString("position");
                String manifesto = rs.getString("manifesto");
                String photo = rs.getString("photo_path");
                boolean approved = rs.getBoolean("approved");
                String nationalIdPath = rs.getString("national_id_path");
                String clearanceCertPath = rs.getString("clearance_certificate_path");
                String academicCertPath = rs.getString("academic_cert_path");
                String nationalIdNumber = rs.getString("national_id");

                HBox candidateCard = new HBox(20);
                candidateCard.setAlignment(Pos.CENTER_LEFT);
                candidateCard.setPadding(new Insets(15));
                candidateCard.setStyle(
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
                        img.setFitWidth(100);
                        img.setFitHeight(100);
                        img.setPreserveRatio(true);
                        img.setStyle("-fx-border-color: #ccc; -fx-border-width: 1; -fx-border-radius: 5;");
                    } catch (Exception ex) {
                        System.err.println("Error loading image for candidate ID " + id + ": " + ex.getMessage());
                        img.setImage(null);
                    }
                }

                VBox detailsBox = new VBox(5);
                Label nameLabel = new Label(name);
                nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #007bff;");
                Label positionLabel = new Label(position + " | Party: " + party);
                positionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
                
                TextArea manifestoBox = new TextArea(manifesto);
                manifestoBox.setEditable(false);
                manifestoBox.setWrapText(true);
                manifestoBox.setMaxHeight(80);
                manifestoBox.setStyle(
                    "-fx-control-inner-background: #e9ecef; " +
                    "-fx-font-size: 12px; " +
                    "-fx-text-fill: #333; " +
                    "-fx-border-color: transparent;"
                );
                
                Label statusLabel = new Label();
                if (approved) {
                    statusLabel.setText("Status: Approved");
                    statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
                } else {
                    statusLabel.setText("Status: Pending/Denied");
                    statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc3545;");
                }

                detailsBox.getChildren().addAll(nameLabel, positionLabel, manifestoBox, statusLabel);
                
                Button approve = new Button("Approve");
                approve.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
                
                Button deny = new Button("Deny");
                deny.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white;");
                
                Button edit = new Button("Edit");
                edit.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black;");
                
                Button delete = new Button("Delete");
                delete.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white;");

                Button downloadNatId = new Button("Download National ID");
                downloadNatId.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                downloadNatId.setDisable(nationalIdPath == null || nationalIdPath.isEmpty());
                downloadNatId.setOnAction(e -> downloadDoc(stage, nationalIdPath, "National_ID_" + nationalIdNumber));

                Button downloadClearance = new Button("Download Clearance Cert");
                downloadClearance.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                downloadClearance.setDisable(clearanceCertPath == null || clearanceCertPath.isEmpty());
                downloadClearance.setOnAction(e -> downloadDoc(stage, clearanceCertPath, "Clearance_Cert_" + nationalIdNumber));

                Button downloadAcademic = new Button("Download Academic Cert");
                downloadAcademic.setStyle("-fx-background-color: #17a2b8; -fx-text-fill: white;");
                downloadAcademic.setDisable(academicCertPath == null || academicCertPath.isEmpty());
                downloadAcademic.setOnAction(e -> downloadDoc(stage, academicCertPath, "Academic_Cert_" + nationalIdNumber));

                HBox actionButtons = new HBox(10, approve, deny, edit, delete);
                actionButtons.setAlignment(Pos.CENTER_RIGHT);

                VBox downloadButtons = new VBox(5, downloadNatId, downloadClearance, downloadAcademic);
                downloadButtons.setAlignment(Pos.CENTER_RIGHT);

                VBox allButtonsBox = new VBox(10, actionButtons, downloadButtons);
                allButtonsBox.setAlignment(Pos.CENTER_RIGHT);
                VBox.setVgrow(allButtonsBox, Priority.ALWAYS);
                HBox.setHgrow(allButtonsBox, Priority.ALWAYS);

                approve.setDisable(approved);
                deny.setDisable(!approved);

                approve.setOnAction(e -> updateStatus(id, 1, container, candidateCard));
                deny.setOnAction(e -> updateStatus(id, 0, container, candidateCard));
                delete.setOnAction(e -> deleteCandidate(id, container, candidateCard));
                edit.setOnAction(e -> showEditDialog(id, container, stage));

                candidateCard.getChildren().addAll(img, detailsBox, allButtonsBox);
                container.getChildren().add(candidateCard);
            }
        } catch (Exception e) {
            container.getChildren().add(new Label("Error loading candidates. " + e.getMessage()));
            e.printStackTrace();
        }

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f2f5;");
        return scrollPane;
    }

    private ScrollPane getVoterContent() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(20));
        container.setStyle("-fx-background-color: #ffffff;");

        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT full_name, national_id FROM voters");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String name = rs.getString("full_name");
                String id = rs.getString("national_id");
                
                Label voterLabel = new Label(name + " - " + id);
                voterLabel.setStyle(
                    "-fx-font-size: 14px; " +
                    "-fx-padding: 8; " +
                    "-fx-background-color: #e9ecef; " +
                    "-fx-border-radius: 5; " +
                    "-fx-background-radius: 5;"
                );
                container.getChildren().add(voterLabel);
            }
        } catch (Exception e) {
            container.getChildren().add(new Label("Error loading voters."));
            e.printStackTrace();
        }

        ScrollPane scrollPane = new ScrollPane(container);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #f0f2f5;");
        return scrollPane;
    }

    private void updateStatus(int id, int newStatus, VBox container, HBox candidateCard) {
        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("UPDATE candidates SET approved = ? WHERE candidate_id = ?");
            stmt.setInt(1, newStatus);
            stmt.setInt(2, id);
            stmt.executeUpdate();
            
            // Find the VBox containing the details (second child of HBox)
            Node detailsNode = candidateCard.getChildren().get(1);
            if (detailsNode instanceof VBox) {
                VBox detailsBox = (VBox) detailsNode;
                for (Node child : detailsBox.getChildren()) {
                    if (child instanceof Label && ((Label) child).getText().startsWith("Status:")) {
                        Label statusLabel = (Label) child;
                        if (newStatus == 1) {
                            statusLabel.setText("Status: Approved");
                            statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #28a745;");
                        } else {
                            statusLabel.setText("Status: Pending/Denied");
                            statusLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc3545;");
                        }
                        break;
                    }
                }
            }

            // Find the VBox containing all buttons (third child of HBox)
            Node buttonsNode = candidateCard.getChildren().get(2);
            if (buttonsNode instanceof VBox) {
                VBox allButtonsBox = (VBox) buttonsNode;
                // Find the HBox containing action buttons (first child of allButtonsBox)
                Node actionButtonsNode = allButtonsBox.getChildren().get(0);
                if (actionButtonsNode instanceof HBox) {
                    HBox actionButtonsBox = (HBox) actionButtonsNode;
                    for (Node child : actionButtonsBox.getChildren()) {
                        if (child instanceof Button) {
                            Button btn = (Button) child;
                            if (btn.getText().equals("Approve")) {
                                btn.setDisable(newStatus == 1);
                            } else if (btn.getText().equals("Deny")) {
                                btn.setDisable(newStatus == 0);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteCandidate(int id, VBox container, HBox candidateCard) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Deletion");
        alert.setHeaderText("Delete Candidate?");
        alert.setContentText("Are you sure you want to delete this candidate? This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try (Connection conn = Database.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM candidates WHERE candidate_id = ?");
                stmt.setInt(1, id);
                stmt.executeUpdate();
                
                container.getChildren().remove(candidateCard);
                
                showAlert("Success", "Candidate has been deleted.");
            } catch (Exception e) {
                showAlert("Error", "An error occurred while deleting the candidate.");
                e.printStackTrace();
            }
        }
    }

    private void showEditDialog(int id, VBox container, Stage stage) {
        Stage dialog = new Stage();
        dialog.setTitle("Edit Candidate");
        
        VBox box = new VBox(15);
        box.setPadding(new Insets(20));
        box.setStyle("-fx-background-color: #f8f9fa;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField partyField = new TextField();
        partyField.setPromptText("Party");
        TextField positionField = new TextField();
        positionField.setPromptText("Position");
        TextArea manifestoField = new TextArea();
        manifestoField.setPromptText("Manifesto");
        manifestoField.setWrapText(true);
        
        nameField.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 5;");
        partyField.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 5;");
        positionField.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 5;");
        manifestoField.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5; -fx-padding: 5;");

        try (Connection conn = Database.getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM candidates WHERE candidate_id = ?");
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nameField.setText(rs.getString("full_name"));
                partyField.setText(rs.getString("party"));
                positionField.setText(rs.getString("position"));
                manifestoField.setText(rs.getString("manifesto"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button save = new Button("Save Changes");
        save.setStyle("-fx-background-color: #007bff; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 15;");
        save.setOnAction(e -> {
            try (Connection conn = Database.getConnection()) {
                PreparedStatement stmt = conn.prepareStatement("UPDATE candidates SET full_name=?, party=?, position=?, manifesto=? WHERE candidate_id=?");
                stmt.setString(1, nameField.getText());
                stmt.setString(2, partyField.getText());
                stmt.setString(3, positionField.getText());
                stmt.setString(4, manifestoField.getText());
                stmt.setInt(5, id);
                stmt.executeUpdate();
                dialog.close();
                
                ScrollPane parent = (ScrollPane) container.getParent();
                parent.setContent(getCandidateContent(stage));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        box.getChildren().addAll(
                new Label("Full Name:"), nameField,
                new Label("Party:"), partyField,
                new Label("Position:"), positionField,
                new Label("Manifesto:"), manifestoField,
                save
        );

        Scene scene = new Scene(box, 400, 500);
        dialog.setScene(scene);
        dialog.show();
    }
    
    private void downloadDoc(Stage stage, String sourcePath, String initialFileName) {
        if (sourcePath == null || sourcePath.isEmpty()) {
            showAlert("No Document", "The document path is empty.");
            return;
        }

        File sourceFile = new File(sourcePath);
        if (!sourceFile.exists()) {
            showAlert("File Not Found", "The document file could not be found at the specified path: " + sourcePath);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Candidate Document");
        String extension = "";
        int i = sourceFile.getName().lastIndexOf('.');
        if (i > 0) {
            extension = sourceFile.getName().substring(i);
        }
        fileChooser.setInitialFileName(initialFileName + extension);
        
        File destFile = fileChooser.showSaveDialog(stage);
        
        if (destFile != null) {
            try (FileInputStream fis = new FileInputStream(sourceFile);
                 FileOutputStream fos = new FileOutputStream(destFile)) {
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                showAlert("Success", "Document downloaded successfully!");
            } catch (IOException e) {
                showAlert("Download Error", "An error occurred while saving the file: " + e.getMessage());
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