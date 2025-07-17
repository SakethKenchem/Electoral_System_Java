package main;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import crud.VoterCRUD;
import javafx.scene.Parent;

public class LandingPage extends VBox {

    public LandingPage(Stage stage) {
        // Styling
        setSpacing(20);
        setAlignment(Pos.CENTER);
        setPrefSize(400, 300);

        // Buttons
        Button voterBtn = new Button("Voter");
        Button adminBtn = new Button("Admin");
        Button candidateBtn = new Button("Candidate");

        // Voter button action: go to VoterCRUD GUI
        voterBtn.setOnAction(e -> {
            VoterCRUD voterCRUD = new VoterCRUD(stage);
            Scene voterScene = new Scene((Parent) voterCRUD.getView(), 600, 600);
            stage.setScene(voterScene);
        });

        // Admin button action: placeholder
        adminBtn.setOnAction(e -> {
            PlaceholderPage adminPage = new PlaceholderPage("Admin section coming soon...");
            Scene adminScene = new Scene(adminPage, 400, 300);
            stage.setScene(adminScene);
        });

        // Candidate button action: placeholder
        candidateBtn.setOnAction(e -> {
            PlaceholderPage candidatePage = new PlaceholderPage("Candidate section coming soon...");
            Scene candidateScene = new Scene(candidatePage, 400, 300);
            stage.setScene(candidateScene);
        });

        // Add buttons
        getChildren().addAll(voterBtn, adminBtn, candidateBtn);
    }
}
