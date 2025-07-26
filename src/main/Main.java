package main;

import crud.AdminLogin;
import javafx.application.Application;
import javafx.stage.Stage;
//import login.AdminLogin;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        AdminLogin.show(primaryStage); // Launch the admin login screen
    }

    public static void main(String[] args) {
        launch(args);
    }
}
