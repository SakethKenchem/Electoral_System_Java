package dashboard;

import db.Database;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminDashboard {

    public void showDashboard(Stage stage) {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        Label heading = new Label("Admin Dashboard");
        heading.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        root.setTop(heading);
        BorderPane.setMargin(heading, new Insets(0, 0, 10, 0));
        BorderPane.setAlignment(heading, javafx.geometry.Pos.CENTER);

        TabPane tabPane = new TabPane();

        // Voters Tab
        TableView<String> voterTable = new TableView<>();
        TableColumn<String, String> voterCol = new TableColumn<>("Voter National ID");
        voterCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()));
        voterTable.getColumns().add(voterCol);

        ObservableList<String> voters = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT national_id FROM voters");
            while (rs.next()) {
                voters.add(rs.getString("national_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        voterTable.setItems(voters);
        Tab voterTab = new Tab("Voters", voterTable);

        // Candidates Tab
        TableView<String> candidateTable = new TableView<>();
        TableColumn<String, String> candidateCol = new TableColumn<>("Candidate National ID");
        candidateCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()));
        candidateTable.getColumns().add(candidateCol);

        ObservableList<String> candidates = FXCollections.observableArrayList();
        try (Connection conn = Database.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT national_id FROM candidates");
            while (rs.next()) {
                candidates.add(rs.getString("national_id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        candidateTable.setItems(candidates);
        Tab candidateTab = new Tab("Candidates", candidateTable);

        tabPane.getTabs().addAll(voterTab, candidateTab);
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 500, 400);
        stage.setTitle("Admin Dashboard");
        stage.setScene(scene);
        stage.show();
    }
}
