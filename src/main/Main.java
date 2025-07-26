package main;

import crud.AdminLogin;
import crud.VoterLogin;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Voter System - Select Role");

        Button voterButton = new Button("Voter Login");
        Button adminButton = new Button("Admin Login");

        voterButton.setMinWidth(200);
        adminButton.setMinWidth(200);

        voterButton.setOnAction(e -> {
            try {
                new VoterLogin().start(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        adminButton.setOnAction(e -> {
            try {
                new AdminLogin().show(new Stage());
                primaryStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        VBox layout = new VBox(20, voterButton, adminButton);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(30));

        Scene scene = new Scene(layout, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
