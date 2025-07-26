package main;

import crud.VoterRegister;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        VoterRegister voterRegister = new VoterRegister();
        voterRegister.start(primaryStage); // Launch Voter Register UI
    }

    public static void main(String[] args) {
        launch(args);
    }
}
