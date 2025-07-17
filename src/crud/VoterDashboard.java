package crud;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class VoterDashboard {

    private VBox view;

    public VoterDashboard(Stage stage, String fullName, String nationalId) {
        view = new VBox(20);
        view.setPadding(new Insets(30));
        view.setAlignment(Pos.CENTER);

        Label welcomeLabel = new Label("Welcome, " + fullName + "!");
        Label idLabel = new Label("Your National ID: " + nationalId);

        Button logoutBtn = new Button("Logout");
        logoutBtn.setOnAction(e -> {
            VoterLogin login = new VoterLogin(stage);
            stage.setScene(new Scene(login.getView(), 600, 400));
        });

        view.getChildren().addAll(welcomeLabel, idLabel, logoutBtn);
    }

    public VBox getView() {
        return view;
    }
}
